#!/usr/bin/env python2.7
"""
Broadcast webcam video data via the StreamerServer class. Resolution can be changed
during the broadcast, and it will notify the clients of the resolution. This
happens at the synchronization step, which is right before every image frame.

The script uses two libraries that is not included in the standard
libraries. The first is OpenCV[0] (Intel Open Source Computer Vision Library) 
and the second is numpy[1], which is used in OpenCV.

[0] http://opencv.org
[1] http://www.numpy.org

"""
import socket
import cv2
import time
import math
import threading


class StreamerServer(threading.Thread):
    def __init__(self, vc, width, height):
        """Set up the initial variables and constants."""
        super(StreamerServer, self).__init__()
        # Constants
        self.MCAST_GRP = '224.0.0.1'
        self.MCAST_PORT = 5007
        self.sync_start = '#!-START-!#'
        self.sync_kill = '#!-QUIT-!#'
        self.chunk_length = 4096 # or 1024
        # Set up once
        self.video = vc
        self.sock = None
        # Will change throughout
        self.width = self.new_width = width
        self.height = self.new_height = height
        self.frames_transmitted = 0
        self.resolution_was_changed = False
        self.halt = False
    
    def run(self):
        """Setup the socket, and run the main loop. When the loop stops, print statistics."""
        self.sock = self.set_up_socket()
        start_time = time.time()
        self.loop()
        run_time = time.time() - start_time
        self.output_statistics(run_time)
        #self.cleanup()
    
    def stop(self):
        """Notify the streamer to stop."""
        self.halt = True
    
    def cleanup(self):
        """Close and free various resources."""
        self.sock.close()
    
    def loop(self):
        """Continiously read from the video feed, and transmit the data."""
        while not self.halt:
            # Capture a frame from the webcam.
            retval, frame = self.video.read()
            self.switch_resolution()
            # Resize the frame
            frame = cv2.resize(frame, (self.width, self.height))
            data = self.frame_to_data(frame)
            if retval:
                self.frames_transmitted += self.send_frame(data)
        # Send kill signal to clients
        self.sock.sendto(self.kill_frame(), (self.MCAST_GRP, self.MCAST_PORT))
    
    def send_frame(self, data):
        """Divide a image frame into smaller chunks, and send them."""
        # Send synchronization package, to tell a new frame is beginning.
        self.sock.sendto(self.sync_frame(), (self.MCAST_GRP, self.MCAST_PORT))
        # We divide the frame into chunks, else it will exceed the maximum
        # block length.
        eaten_data = 0
        for i in range(0, self.chunks_to_send()):
            eaten_data = self.chunk_length * i
            data_chunk = data[eaten_data:eaten_data+self.chunk_length]
            self.sock.sendto(data_chunk, (self.MCAST_GRP, self.MCAST_PORT))
        return 1
    
    def frame_to_data(self, frame):
        """Prepare the image frames by resizing them and converting it to a string."""
        # Convert the frame to a string. We zero pad the data string, to
        # fit with a whole number of chunks.
        frame = frame.flatten()
        data = frame.tostring().ljust(self.chunks_to_send() * self.chunk_length, '0')
        return data
    
    def output_statistics(self, run_time):
        """Output various statistics, like run time, received frames and fps."""
        print '\nRun time: %s seconds' % (run_time,)
        print 'Frames sent:', self.frames_transmitted
        print 'Average frame rate: %s fps' % (self.frames_transmitted / run_time,)

    def set_up_socket(self):
        """Set up the initial multicast UDP socket broadcast."""
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 32)
        return sock
    
    def connnect_to_webcam(self):
        """Connect to the webcam video feed."""
        vc = cv2.VideoCapture(0)   
        vc.open(0)
        return vc
    
    def sync_frame(self):
        """Construct the sync frame."""
        sync = '%s:%sx%s:' % (self.sync_start, self.width, self.height,)
        return sync.ljust(self.chunk_length, '0')
    
    def kill_frame(self):
        """Construct the sync frame."""
        return self.sync_kill.ljust(self.chunk_length, '0')
    
    def change_resolution(self, w, h):
        """Prepare the resolution to be changed at next available oppurtunity."""
        self.resolution_was_changed = True
        self.new_width = w
        self.new_height = h
    
    def switch_resolution(self):
        """If the resoulution was changed, switch the current one to the new one."""
        if self.resolution_was_changed:
            self.width = self.new_width
            self.height = self.new_height
            self.resolution_was_changed = False
    
    def full_frame_length(self):
        """Calculate the length of the image frame."""
        return self.height * self.width * 3
    
    def chunks_to_send(self):
        """Calculate the minimum number of whole chunks to send."""
        return int(math.ceil(1.0 * self.full_frame_length() / self.chunk_length))


if __name__=='__main__':
    # For some reason, cv2.VideoCapture can't be controlled inside the class/thread.
    print 'Connecting to webcam...'
    vc = cv2.VideoCapture(0)   
    vc.open(0)
    print 'Starting data transmission...'
    streamer_thread = StreamerServer(vc, 890, 500)
    streamer_thread.daemon = True
    streamer_thread.start()
    
    # Interactive menu
    print 'Resolutions available:'
    print '        1: 1280x720'
    print '        2: 1120x630'
    print '        3: 960x540'
    print '        4: 890x500'
    print '        5: 800x450'
    print '        6: 640x360'
    print '        7: 480x270'
    print '        8: 320x180'
    print '        9: 160x90'
    print '        q: quit'
    while streamer_thread.is_alive():
        inp = raw_input('Choose a resolution: ')
        if inp == '1':
            print 'Changing resolution to %sx%s' % (1280, 720,)
            streamer_thread.change_resolution(1280, 720)
        elif inp == '2':
            print 'Changing resolution to %sx%s' % (1120, 630,)
            streamer_thread.change_resolution(1120, 630)
        elif inp == '3':
            print 'Changing resolution to %sx%s' % (960, 540,)
            streamer_thread.change_resolution(960, 540)
        elif inp == '4':
            print 'Changing resolution to %sx%s' % (890, 500,)
            streamer_thread.change_resolution(890, 500)
        elif inp == '5':
            print 'Changing resolution to %sx%s' % (800, 450,)
            streamer_thread.change_resolution(800, 450)
        elif inp == '6':
            print 'Changing resolution to %sx%s' % (640, 360,)
            streamer_thread.change_resolution(640, 360)
        elif inp == '7':
            print 'Changing resolution to %sx%s' % (480, 270,)
            streamer_thread.change_resolution(480, 270)
        elif inp == '8':
            print 'Changing resolution to %sx%s' % (320, 180,)
            streamer_thread.change_resolution(320, 180)
        elif inp == '9':
            print 'Changing resolution to %sx%s' % (160, 90,)
            streamer_thread.change_resolution(160, 90)
        elif inp == 'q':
            print 'Quitting...'
            streamer_thread.stop()
            streamer_thread.join()
            break
        time.sleep(0.1)
    vc.release()
