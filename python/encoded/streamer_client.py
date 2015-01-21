#!/usr/bin/env python2.7
"""
Connect to a broadcast stream, and display the video feed, using the
StreamerClient class. It automatically detects the resolution used, from
the servers synchronization frames.

The script uses two libraries that is not included in the standard
libraries. The first is OpenCV[0] (Intel Open Source Computer Vision Library) 
and the second is numpy[1], which is used in OpenCV.

[0] http://opencv.org
[1] http://www.numpy.org

"""
import socket
import struct
import cv2
import numpy
import time
import threading


class StreamerClient(threading.Thread):
    def __init__(self, output_file=None, output_size=None, output_format=None):
        """Set up the initial variables and constants."""
        super(StreamerClient, self).__init__()
        # Constants
        self.MCAST_GRP = '224.0.0.1'
        self.MCAST_PORT = 5007
        self.sync_start = '#!-START-!#'
        self.sync_kill = '#!-QUIT-!#'
        self.chunk_length = 4096 # or 1024
        self.sock = None
        # Will change throughout 
        self.width = 0
        self.height = 0
        self.received_frames = 0
        self.halt = False
        self.output_file = output_file
        if output_size is None:
            self.output_size = (1280, 720)
        else:
            self.output_size = output_size
        if output_format is None:
            self.output_format = cv2.VideoWriter_fourcc(*'mpeg')
        else:
            self.output_format = cv2.VideoWriter_fourcc(*output_format)
        self.fps = 20
        self.video_writer = None
        
    def run(self):
        """Setup the socket, and run the main loop. When the loop stops, print statistics."""
        self.sock = self.set_up_socket()
        if self.output_file is None:
            cv2.namedWindow('Video Preview')
        else:
            self.video_writer = cv2.VideoWriter(self.output_file, self.output_format, self.fps, self.output_size)
        start_time = time.time()
        self.loop()
        run_time = time.time() - start_time
        self.output_statistics(run_time)
        self.cleanup()
    
    def stop(self):
        """Notify the streamer to stop."""
        self.halt = True
    
    def cleanup(self):
        """Close and free various resources."""
        self.sock.close()
        cv2.destroyAllWindows()
        if self.video_writer is not None:
            self.video_writer.release()
    
    def loop(self):
        """Continiously read from the data stream, and display the video data."""
        data_received = 0
        data = ''
        start_time = time.time()
        current_start_time = time.time()
        current_frames_received = 0
        while not self.halt:
            # Collect data until we have a full frame.
            tmp_data = self.sock.recv(self.chunk_length)
            data += tmp_data
            if tmp_data.startswith(self.sync_start):
                # Reset data variables, to prepare for a new frame.
                data = ''
                data_received = 0
                switch = self.switch_resolution(tmp_data)
                if switch:
                    current_start_time = time.time()
                    current_frames_received = 0
            elif tmp_data.startswith(self.sync_kill):
                # The server is closing the connection.
                print 'Received kill frame...'
                self.halt = True
            elif self.width != 0 and self.height != 0:
                data_received += self.chunk_length
                # When we've received a frame or more (because of zero padding), 
                # then we can finally show it.
                if data_received >= self.full_frame_length():
                    data = data[:self.full_frame_length()]
                    frame = self.data_to_frame(data)
                    if self.output_file is None:
                        self.show_video_image(frame)
                    else:
                        self.write_video_image(frame)
                    # Update statistics
                    self.received_frames += 1
                    current_frames_received += 1
                    self.output_statistics_during(start_time, current_start_time, current_frames_received)
                    # Reset the values after we've received the whole frame.
                    data_received = 0
                    data = ''
    
    def show_video_image(self, frame):
        """Displays the frame, using OpenCVs window."""
        cv2.imshow('Video Preview', frame)
        cv2.waitKey(10)
    
    def write_video_image(self, frame):
        frame = cv2.resize(frame, self.output_size)
        self.video_writer.write(frame)
    
    def data_to_frame(self, data):
        """Convert the data from a string, to a numpy matrix."""
        frame = numpy.fromstring(data, dtype=numpy.uint8)
        frame = numpy.reshape(frame, (self.height, self.width, 3))
        return frame
    
    def output_statistics_during(self, start_time, current_start_time, current_frames_received):
        run_time = time.time() - start_time
        cur_time = time.time() - current_start_time
        resolution = '%sx%s' % (self.width, self.height,)
        MBps_per_frame = self.full_frame_length() / 1000.0 / 1000.0
        fps = current_frames_received / cur_time
        print '\x1b[1A' + '\x1b[2K' + 'Res: %s, Total - Time: %.2fs, Frames: %s :: Current - Time: %.2fs, Frames: %s, FPS: %.2f, MBps: %.2f' % (
            resolution,
            run_time,
            self.received_frames,
            cur_time,
            current_frames_received,
            fps,
            MBps_per_frame * fps,
        )
    
    def output_statistics(self, run_time):
        """Output various statistics, like run time, received frames, fps and MBps."""
        fps = self.received_frames / run_time
        MBps_per_frame = self.full_frame_length() / 1000.0 / 1000.0
        print '\nRun time: %.2f seconds' % (run_time,)
        print 'Received frames: ', self.received_frames
        print 'Avg. frame rate: %s fps' % (fps,)
        print 'Avg. Bit rate: %.2f MB/s' % (MBps_per_frame * fps,)
    
    def set_up_socket(self):
        """Set up the initial multicast UDP socket connection."""
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind((self.MCAST_GRP, self.MCAST_PORT))
        mreq = struct.pack('4sl', socket.inet_aton(self.MCAST_GRP), socket.INADDR_ANY)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
        return sock
        
    def switch_resolution(self, sync_frame):
        """If the resoulution was changed, switch the current one to the new one."""
        w, h = sync_frame.split(':')[1].split('x')
        if int(h) != self.height and int(w) != self.width:
            self.width = int(w)
            self.height = int(h)
            return True
    
    def full_frame_length(self):
        """Calculate the length of the image frame."""
        return self.height * self.width * 3
    

if __name__=='__main__':
    # Use the following codecs: 
    #   MPEG-4 : fmp4 (ffmpeg), xvid 
    #   MPEG-1 : mpeg
    print 'Setting up connection...'
    streamer_client = StreamerClient(output_file='TestVideoData/output.avi', output_format='xvid')
    print 'Beginning to listen for data...'
    streamer_client.run()
