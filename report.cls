\ProvidesClass{report}

\LoadClass[12pt,a4paper]{article}

% packages that are regularly used
\usepackage{xcolor}
\usepackage{fontspec}
\usepackage{amsmath}
\usepackage{amssymb}
\usepackage{listings}
%\usepackage{float}
\usepackage{tikz}
\usepackage[danish,english]{babel}
\usepackage{fixltx2e}
\usepackage{lastpage}
\usepackage{truncate}
\usepackage{minted}
\usepackage[a4paper, top=3cm, bottom=3cm, left=2cm, right=2cm]{geometry}

\definecolor{c991a14}{RGB}{153,26,20}
\definecolor{c999999}{RGB}{153,153,153}

%--------------------------------------------------------
% Links style
%--------------------------------------------------------
\usepackage{graphicx,colortbl}
\usepackage{fontspec}
\usepackage[colorlinks = true,
            linkcolor = black,
            urlcolor  = c991a14,
            citecolor = black,
            anchorcolor = black]{hyperref}
            

%--------------------------------------------------------
% Fields that user can set (in addition to title, author, etc)
%--------------------------------------------------------
\newcommand{\course}[1]{\def \course {#1}}
\newcommand{\titlepagepicture}[1]{\def \titlepagepicture {#1}}
\makeatletter
\newcommand{\candidatenum}[1]{\def \@candidatenum {#1}}
\newcommand{\subtitle}[1]{\def \@subtitle {#1}}
\newcommand{\subject}[1]{\def \@subject {#1}}
\newcommand{\characters}[1]{\def \@characters {#1}}
\newcommand{\email}[1]{\def \@email {#1}}
\newcommand{\supervisor}[1]{\def \@supervisor {#1}}
\candidatenum{}
\subtitle{}
\subject{}
\characters{}
\email{}
\supervisor{}
\makeatother


%--------------------------------------------------------
% Headers and footers
%--------------------------------------------------------
\usepackage{fancyhdr}
\pagestyle{fancy}
\makeatletter
\lhead{\truncate{200pt}{\course}}
\rhead{\@author}
\chead{\@date}
\cfoot{}
\rfoot{page \thepage\ of \pageref{LastPage}}


%--------------------------------------------------------
% Commonly used combination of commands and environments
%--------------------------------------------------------
\newcommand{\ToC}[1][Table of Contents]{\
    \renewcommand*\contentsname{#1}
    \tableofcontents
    \thispagestyle{empty}
    \newpage
    \setcounter{page}{1}
}

\newcommand{\ToCWithFigures}[1][Table of Contents]{\
    \renewcommand*\contentsname{#1}
    \tableofcontents
    \listoffigures
    \thispagestyle{empty}
    \newpage
    \setcounter{page}{1}
}

\newcommand{\ToCWithTables}[1][Table of Contents]{\
    \renewcommand*\contentsname{#1}
    \tableofcontents
    \listoftables
    \thispagestyle{empty}
    \newpage
    \setcounter{page}{1}
}

\newcommand{\ToCWithTablesAndFigures}[1][Table of Contents]{\
    \renewcommand*\contentsname{#1}
    \tableofcontents
    \listoftables
    \listoffigures
    \thispagestyle{empty}
    \newpage
    \setcounter{page}{1}
}

\newcommand{\Section}[1]{\
    \section{#1}\label{sec:#1}
    \input{Section/#1}
}

\newenvironment{subeq}{
    \subequations
        \align
}{
        \endalign
    \endsubequations
}

\newenvironment{Table}[3]{
    \def\tempcaption{#2}
    \def\templabel{#3}
    \begin{table}[H]
        \begin{center}
        \begin{tabular}{#1}
}{
        \end{tabular}
        \end{center}
        \caption{\tempcaption}
        \label{tab:\templabel}
    \end{table}
}

\newcommand{\Fig}[3][1]{ 
    \begin{figure}[H] 
        \centering 
        \includegraphics[width=#1\textwidth]{Figure/#2} 
        \caption{#3} 
        \label{fig:#2} 
    \end{figure} 
}

\newcounter{AppendixNumbering}
\newenvironment{Appendix}[1]{
    \stepcounter{AppendixNumbering}
    \newpage
    \addcontentsline{toc}{section}{Appendix \Alph{AppendixNumbering}: #1}
    \section*{Appendix \Alph{AppendixNumbering}: #1}
}{}

\newenvironment{Abstract}{
    \thispagestyle{empty}
    {\centering\section*{Abstract}}
}{
    \newpage
}


%--------------------------------------------------------
% For a semi nice looking \lstlisting (code highlighting)
%--------------------------------------------------------
\definecolor{lightlightgray}{gray}{0.95} % background
\definecolor{shred}{rgb}{0.6,0,0} % for strings
\definecolor{shgreen}{rgb}{153, 153, 136} % comments
\definecolor{shpurple}{rgb}{0.5,0,0.35} % keywords

\lstset{
  breaklines=true,
  xleftmargin=25pt,
  xrightmargin=25pt,
  aboveskip=10pt,
  belowskip=10pt,
  basicstyle=\ttfamily,
  backgroundcolor=\color{lightlightgray},
  showstringspaces=false,
  frame=ltrb,
  language=Matlab,
  tabsize=2,
  numbers=left,
  numberstyle=\small,
  numbersep=8pt,
  morekeywords={*, factorial, sum, erlang},
  keywordstyle=\color{shpurple}\textbf,
  commentstyle=\color{black}\textit,
  stringstyle=\color{shred}
}


%--------------------------------------------------------
% Construct the front page
%--------------------------------------------------------
\makeatother
\makeatletter
\renewcommand{\maketitle}{
  \begin{titlepage}
    \begin{center}
      \vspace*{0.5cm}
      \begin{tikzpicture}[y=0.80pt, x=0.8pt,yscale=-1, scale=0.5, inner sep=0pt, outer sep=0pt]
        \begin{scope}[shift={(0,147.63782)}]
          \path[fill=c991a14] (1048.0000,405.3696) .. controls (1048.0000,405.3696) and
          (1071.0924,393.0689) .. (1084.0000,386.3622) .. controls (1120.0000,402.3622)
          and (1140.0000,402.3696) .. (1176.0000,386.3692) -- (1212.0000,405.3692) --
          (1176.0000,424.3617) .. controls (1140.0000,408.3617) and (1120.0000,408.3692)
          .. (1084.0540,424.3617) -- cycle;
          \path[fill=c999999] (1160.1406,353.3139) .. controls (1157.8085,349.5147) and
          (1157.9229,341.3857) .. (1157.9229,341.3857) -- (1157.9609,276.4391) --
          (1164.9609,276.4391) -- (1171.9609,276.4391) -- (1171.9859,343.1704) ..
          controls (1171.9419,349.4460) and (1174.9506,353.4771) .. (1180.5101,353.3793)
          .. controls (1186.0695,353.2815) and (1188.9912,349.4034) ..
          (1188.9912,343.1720) -- (1189.0012,276.3622) -- (1203.0012,276.3622) --
          (1202.8907,341.1264) .. controls (1202.8907,341.1264) and (1203.5281,348.6496)
          .. (1200.8017,352.9391) .. controls (1193.9764,364.3049) and
          (1180.9875,363.4287) .. (1180.9875,363.4287) .. controls (1180.9875,363.4287)
          and (1167.2514,364.8963) .. (1160.1418,353.3139) -- cycle;
          \path[fill=c999999] (1079.0000,276.5809) .. controls (1077.0720,276.5654) and
          (1075.0465,276.5997) .. (1072.9687,276.6434) -- (1058.4687,276.9247) --
          (1058.1875,318.4247) .. controls (1058.0438,341.2493) and (1058.0000,361.3622)
          .. (1058.4375,361.2684) .. controls (1058.8442,362.3282) and
          (1062.3813,362.5348) .. (1075.4062,362.2684) .. controls (1090.1344,361.9672)
          and (1092.1204,361.7107) .. (1094.5937,359.8622) .. controls
          (1098.5809,356.8825) and (1100.9620,353.3376) .. (1102.0000,348.8622) ..
          controls (1103.2767,343.3573) and (1103.2005,293.8351) .. (1101.9060,289.1747)
          .. controls (1100.4881,284.0690) and (1095.5770,278.6928) ..
          (1091.1247,277.3934) .. controls (1089.5526,276.9346) and (1084.7839,276.6274)
          .. (1078.9997,276.5809) -- cycle(1072.9687,286.8502) -- (1078.5129,286.8944)
          .. controls (1090.5687,286.5671) and (1089.0696,297.1226) ..
          (1088.7812,326.7372) .. controls (1088.7812,342.0163) and (1090.3264,351.9036)
          .. (1078.2187,351.9992) -- (1072.7478,351.9992) -- (1072.9687,319.4247) --
          cycle;
          \path[fill=c999999] (1123.0000,362.3622) -- (1122.9620,325.3949) --
          (1123.0000,287.3622) -- (1108.0000,287.3622) -- (1108.0000,276.3622) --
          (1153.0000,276.3622) -- (1153.0000,287.3622) -- (1138.0000,287.3622) --
          (1137.9620,324.9391) -- (1138.0000,362.3622) -- cycle;
          \path[fill=c991a14] (1048.0000,450.3692) .. controls (1048.0000,450.3692) and
          (1071.0924,438.0684) .. (1084.0000,431.3617) .. controls (1120.0000,447.3617)
          and (1140.0000,447.3692) .. (1176.0000,431.3697) -- (1212.0000,450.3697) --
          (1176.0000,469.3622) .. controls (1140.0000,453.3622) and (1120.0000,453.3697)
          .. (1084.0540,469.3622) -- cycle;
          \path[fill=c991a14] (1048.0000,495.3697) .. controls (1048.0000,495.3697) and
          (1071.0924,483.0689) .. (1084.0000,476.3622) .. controls (1120.0000,492.3622)
          and (1140.0000,492.3697) .. (1176.0000,476.3702) -- (1212.0000,495.3702) --
          (1176.0000,514.3627) .. controls (1140.0000,498.3627) and (1120.0000,498.3702)
          .. (1084.0540,514.3627) -- cycle;
        \end{scope}

      \end{tikzpicture}\\
      \vspace{1.5cm}
      {\LARGE Technical University of Denmark}
      \vspace{3cm}
      \hrule
      \vspace{0.6cm}
      \textsc{\LARGE \@title}
      \vspace{0.6cm}
      \hrule
      \vspace{0.6cm}
      \textsc{Course:}\\
      \course\\
      \vspace{0.6cm}
      \vspace{2cm}
    \end{center}

    \begin{minipage}[t]{0.7\textwidth}
      \begin{flushleft} \large
        \textsc{Author(s):}\\
        \vspace{0.15cm}
        \@author
      \end{flushleft}
    \end{minipage}
    \begin{minipage}[t]{0.2\textwidth}
      \begin{flushright} \large
        \ifx\@supervisor\empty\else
          \textsc{Supervisor(s):} \@supervisor\\
          \vspace{0.15cm}
        \fi
        \ifx\@characters\empty\else
          \textsc{Characters:} \@characters
        \fi
      \end{flushright}
    \end{minipage}
    \vfill
    \begin{center}
      \@date
    \end{center}
    \thispagestyle{empty}
    \setcounter{page}{0}
  \end{titlepage}
}
\makeatother

\usetikzlibrary{%
  calc,%
  decorations.pathmorphing,%
  fadings,%
  shadings%
}
