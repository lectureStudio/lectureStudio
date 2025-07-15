# Changelog

All notable changes to this project will be documented in this file.

## [6.2.2](https://github.com/lectureStudio/lectureStudio/compare/v6.2.1..v6.2.2) - 2025-07-11

### Bug Fixes

- *(presenter)* Set more default buttons in the toolbar
- *(presenter)* Scale incoming video frames with AffineTransformOp
- *(presenter)* Buffer video frames for conversion
- *(presenter)* Remaining video tiles after closing a ([#1012](https://github.com/lectureStudio/lectureStudio/issues/1012))
- Get document title after the document was ([#1015](https://github.com/lectureStudio/lectureStudio/issues/1015))
- *(presenter)* Add debug logging for presentation view updates and screen handling
- *(editor)* Consume mouse event on selection change in the ([#936](https://github.com/lectureStudio/lectureStudio/issues/936))
- *(presenter)* Adjust heartbeat interval and duration
- *(editor)* Adjust stroke widths for pen, highlighter, and ([#1020](https://github.com/lectureStudio/lectureStudio/issues/1020))
- *(editor)* Improve seek ([#1023](https://github.com/lectureStudio/lectureStudio/issues/1023))
- *(editor)* Fix saving single ([#1021](https://github.com/lectureStudio/lectureStudio/issues/1021))
- *(editor)* Improve event handling on recording changes
- *(presenter)* Improve video frame handling and memory management
- *(presenter)* Optimize video frame conversion and memory ([#1018](https://github.com/lectureStudio/lectureStudio/issues/1018))
- Memory leak caused by screen-share while ([#1018](https://github.com/lectureStudio/lectureStudio/issues/1018))
- Set up WiX in GitHub workflows
- WiX toolset extension versions
- Wix-module fail maven build on error
- Build workflow to upload only the lectureStudio exe bundle
- *(editor)* Scaling and positioning of recorded video frames
- *(editor)* Select video export option by default in VideoExportPresenter
- Unnecessarily setting the current recording to ([#1019](https://github.com/lectureStudio/lectureStudio/issues/1019))
- Undo/redo for the text ([#29](https://github.com/lectureStudio/lectureStudio/issues/29))
- Improve access token validation and error handling in stream ([#380](https://github.com/lectureStudio/lectureStudio/issues/380))
- *(editor)* Seek state handling in ([#507](https://github.com/lectureStudio/lectureStudio/issues/507))
- *(presenter)* Fix main view repaint after showing stacked dialogs
- *(presenter)* Show notify to record notification while in ([#1028](https://github.com/lectureStudio/lectureStudio/issues/1028))
- *(editor)* View update caused by regression in fix ([#1019](https://github.com/lectureStudio/lectureStudio/issues/1019)) ([#1027](https://github.com/lectureStudio/lectureStudio/issues/1027))
- *(editor)* View update regression when changing recorded events
- *(presenter)* Closing heartbeat notification
- *(presenter)* Add logging for heartbeat request failures and improve event handling
- *(presenter)* Auto grid on external displays
- *(presenter)* Update phrasing for speech ([#1032](https://github.com/lectureStudio/lectureStudio/issues/1032))
- *(core)* Screen handling logic and enhance ([#1031](https://github.com/lectureStudio/lectureStudio/issues/1031))
- *(core)* Compare in logical display space instead of pixel space
- Typo in PageEditEvent
- *(core)* Add shape paint event on shape removal
- *(tests)* Update Screen initialization
- *(core)* Handle exception during buffer release in ([#884](https://github.com/lectureStudio/lectureStudio/issues/884))
- *(core)* Ensure legal file names in file chooser and recording processes

### Refactor

- ScreenRecorderService to drop the ffmpeg cli muxer and added FFmpegMuxer implemented with JavaCV

### Other

- New windows msi/exe bundle creation
- Update GitHub workflows to use the new windows bundle
- *(deps)* Bump org.springframework:spring-context in /lect-web-api ([#1022](https://github.com/lectureStudio/lectureStudio/issues/1022))
- Allow opening files with drag&drop ([#1026](https://github.com/lectureStudio/lectureStudio/issues/1026))
- Update Windows runner version to 2022 in CI configurations

### Documentation

- Presentation view java docs
- Recording player and service documentation
- Enhance documentation for event execution classes and methods
- Improve documentation and clarify method descriptions for the presentation context

### Miscellaneous Tasks

- Drop dead code

## [6.2.1](https://github.com/lectureStudio/lectureStudio/compare/v6.2.0..v6.2.1) - 2025-04-22

### Bug Fixes

- Start stream with muted microphone
- Do not react to VAD when settings are opened
- Audio device changes for voice activity detection
- Process local video frames when the connection is established

### Miscellaneous Tasks

- *(release)* Update CHANGELOG.md for v6.2.1
- *(release)* Update package version to 6.2.1

## [6.2.0](https://github.com/lectureStudio/lectureStudio/compare/v6.1.1025..v6.2.0) - 2025-04-15

### Features

- Native JavaFX file/directory choosers ([#997](https://github.com/lectureStudio/lectureStudio/issues/997))
- Show participant cam video in tab and external window ([#998](https://github.com/lectureStudio/lectureStudio/issues/998))
- A different trigger for the recording alert as described ([#991](https://github.com/lectureStudio/lectureStudio/issues/991)) ([#1006](https://github.com/lectureStudio/lectureStudio/issues/1006))

### Bug Fixes

- MacOS specific switches for compatibility reasons
- Missing recorded PDF slides and removed recording warn ([#986](https://github.com/lectureStudio/lectureStudio/issues/986))
- Executing keystroke actions in text ([#994](https://github.com/lectureStudio/lectureStudio/issues/994))
- Missing annotations when the recording was ([#982](https://github.com/lectureStudio/lectureStudio/issues/982))
- Show participants list instead of chat messages on stream ([#989](https://github.com/lectureStudio/lectureStudio/issues/989))
- Slide recording eliminating native code calls ([#996](https://github.com/lectureStudio/lectureStudio/issues/996))
- Speaker layout by taking last talking timestamp into account
- Participant speech request icons
- Ui layout glitches around video ([#1002](https://github.com/lectureStudio/lectureStudio/issues/1002))
- Wire JanusParticipantContext to the ([#1003](https://github.com/lectureStudio/lectureStudio/issues/1003))
- Memory leak with video frame ([#1000](https://github.com/lectureStudio/lectureStudio/issues/1000)) ([#1001](https://github.com/lectureStudio/lectureStudio/issues/1001))
- Crashes caused by video frame ([#1000](https://github.com/lectureStudio/lectureStudio/issues/1000)) ([#1001](https://github.com/lectureStudio/lectureStudio/issues/1001))
- ParticipantList speech request cancellation
- *(editor)* Prevent the slide preview from triggering scrolling ([#983](https://github.com/lectureStudio/lectureStudio/issues/983))
- Editor crash when opening a recording with a screen-([#985](https://github.com/lectureStudio/lectureStudio/issues/985))
- *(editor)* Crash caused by wrong libstdc++ the openjdk and javafx have linked
- *(build)* Removed deprecation warnings for builds
- *(editor)* Rendering recorded screen-shares in linux
- *(editor)* Rendering recorded screen-shares in macOS
- Updated to mupdf 1.25.5 with customized linux build to fix black boxes
- Mupdf 1.25.5 lib build with -Bsymbolic ldflags
- Minor screen-share fixes
- Screen-share on connected displays
- Recording warning triggers
- ManualStateObserver property propagation
- Create quiz logic with closed generic document
- Rendering imported PDF pages using ([#1008](https://github.com/lectureStudio/lectureStudio/issues/1008))
- Start recording with muted microphone shows alert

### Refactor

- Display notification handler

### Other

- Make macOS package.sh use the notarytool for notarization
- Added GitHub build and release workflows

### Documentation

- Added the complete changelog so far
- Quiz related docs

### Testing

- Fixed module tests by correctly loading the native MuPDF lib

### Miscellaneous Tasks

- Generate the current version for the build workflow
- Fetch-depth 0 in the build workflow to get the current version
- Added MuPDF build workflow
- *(release)* Update CHANGELOG.md for v6.2.0
- *(release)* Update package version to 6.2.0

## [6.1.1025](https://github.com/lectureStudio/lectureStudio/compare/v6.1.963..v6.1.1025) - 2024-12-03

### Presenter

- Fixed menu state for ([#379](https://github.com/lectureStudio/lectureStudio/issues/379))
- Fixed stream and messenger ([#379](https://github.com/lectureStudio/lectureStudio/issues/379))
- Fixed recording path ([#733](https://github.com/lectureStudio/lectureStudio/issues/733))
- Fixed restarting minimized windows for ([#954](https://github.com/lectureStudio/lectureStudio/issues/954))
- Fixed recording state propagation on stream ([#935](https://github.com/lectureStudio/lectureStudio/issues/935))
- Fixed stopping all screen recordings
- Fixed speech ([#967](https://github.com/lectureStudio/lectureStudio/issues/967))
- Fixed speech menu selected ([#967](https://github.com/lectureStudio/lectureStudio/issues/967))
- Updated jcef dependency
- Show error on record page failure
- Fixed MuPDFDocument page recording index

### Presenter API

- Fixed RecordingService state when audio input is ([#926](https://github.com/lectureStudio/lectureStudio/issues/926))
- Use AudioDeviceChangeListener in SoundSettingsPresenter
- Query audio devices asynchronously
- Scroll slides with mouse ([#916](https://github.com/lectureStudio/lectureStudio/issues/916))
- Fixed status display of the recording in the ([#933](https://github.com/lectureStudio/lectureStudio/issues/933))
- Added screen inhibition config option
- Refactored screen inhibition config usage
- Added stream service ([#969](https://github.com/lectureStudio/lectureStudio/issues/969))
- Use janus-ws endpoint for janus websockets
- Minor fixes to Heartbeat and Stopwatch
- Show Stopwatch config error in the UI
- Fixed ui hanging after opening a document from the command line
- Close heartbeat notification on reconnected notification

### Editor

- Removed unnecessary dependencies
- Render video frames in the SlidesView
- Improved rendering video frames in the SlidesView
- Fast seeking video keyframes
- Fixed opening recording files via double ([#945](https://github.com/lectureStudio/lectureStudio/issues/945))
- Fixed position marker ([#949](https://github.com/lectureStudio/lectureStudio/issues/949))
- Fixed timestamp and frame waiting time ([#948](https://github.com/lectureStudio/lectureStudio/issues/948))
- Fixed playing paused screen ([#948](https://github.com/lectureStudio/lectureStudio/issues/948))
- Fixed unnecessary playback of videos
- Slower but precise video ([#946](https://github.com/lectureStudio/lectureStudio/issues/946))
- Init native libraries for video playback in ([#947](https://github.com/lectureStudio/lectureStudio/issues/947))
- Reset state after closing a ([#951](https://github.com/lectureStudio/lectureStudio/issues/951))
- Fixed media tracks sliders after ([#951](https://github.com/lectureStudio/lectureStudio/issues/951))
- Handle video render errors
- Fixed annotation over video rendering
- Fixed page image rendering after end of video
- Drop ProgressDialogView in favor of ([#959](https://github.com/lectureStudio/lectureStudio/issues/959))
- Fixed video seeking by taking performance ([#957](https://github.com/lectureStudio/lectureStudio/issues/957))
- Do not include static actions into inserted slides
- Fixed time marker position after ([#973](https://github.com/lectureStudio/lectureStudio/issues/973))
- Fixed pdf slide ([#976](https://github.com/lectureStudio/lectureStudio/issues/976))

### Editor API

- Reduced font size for page labels in the event-([#927](https://github.com/lectureStudio/lectureStudio/issues/927))
- Added configurable actions unite ([#898](https://github.com/lectureStudio/lectureStudio/issues/898))
- Reload events table when the unite threshold ([#898](https://github.com/lectureStudio/lectureStudio/issues/898))
- Fixed slide repaint on on-time seek
- Screen share video rendering ([#937](https://github.com/lectureStudio/lectureStudio/issues/937))
- Added scroll event handling in the main slides ([#932](https://github.com/lectureStudio/lectureStudio/issues/932))
- Added feature to insert PDF pages
- Fixed audio clicks at the beginning of the ([#977](https://github.com/lectureStudio/lectureStudio/issues/977))

### Core

- Fixed slide text sort contract issue

### Other

- Added screen action
- Fixed javafx conflicting versions
- Fixed macOS builds by using notarytool
- Fixed log4j version
- Updated dependencies having reported CVEs

## [6.1.963](https://github.com/lectureStudio/lectureStudio/compare/v6.1.x..v6.1.963) - 2024-05-28

### Presenter API

- Added shortcuts and manual menu items
- Added missing shortcuts to the ShortcutsView
- Added shortcut filtering to the ShortcutsView
- Fixed minor shortcut keys
- Added Bachelorpraktikum chat extension
- Fixed edited chat message to slide conversion
- Fixed showing external window when the screen does not exist
- Minor translation fixes
- Fixed opening whiteboard via ([#894](https://github.com/lectureStudio/lectureStudio/issues/894))
- Fixed slide overlay detection for some slide ([#889](https://github.com/lectureStudio/lectureStudio/issues/889))
- Fixed missing default access link in publisher api
- Disable focus for tabbed panes
- Fixed text annotation font size
- Show error notification if a slide could not be ([#912](https://github.com/lectureStudio/lectureStudio/issues/912))
- Improved whitespaces in ([#903](https://github.com/lectureStudio/lectureStudio/issues/903))
- Fixed frame size for the external preview ([#919](https://github.com/lectureStudio/lectureStudio/issues/919))
- Save note slide ([#919](https://github.com/lectureStudio/lectureStudio/issues/919))
- Expand slide notes tab when notes are ([#919](https://github.com/lectureStudio/lectureStudio/issues/919))
- Fixed recent documents list ([#915](https://github.com/lectureStudio/lectureStudio/issues/915))
- Improved DocButton ([#915](https://github.com/lectureStudio/lectureStudio/issues/915)) and fixed NPE with closed bookmarks
- Fixed linux compat issue with ([#915](https://github.com/lectureStudio/lectureStudio/issues/915))
- Reduced text-box fonts to only embedded ones
- Changed default font for MessageDocument
- Remind to enable external displays on presentation start
- Fixed wrong font size in text-boxes
- Fixed text-boxes disposal
- Fixed restoring slide-preview ([#919](https://github.com/lectureStudio/lectureStudio/issues/919))
- Center video feed in PeerView
- Fixed line/word breaking in ([#922](https://github.com/lectureStudio/lectureStudio/issues/922))

### Editor API

- Added manual menu item
- Fixed event table ([#921](https://github.com/lectureStudio/lectureStudio/issues/921))
- Fixed text font issues with custom text on ([#910](https://github.com/lectureStudio/lectureStudio/issues/910))
- Fixed audio render progress ([#908](https://github.com/lectureStudio/lectureStudio/issues/908))

### Other

- Added linux deb and rpm package support
- Updated manual url
- Feature/guest ([#885](https://github.com/lectureStudio/lectureStudio/issues/885))

## [6.1.x](https://github.com/lectureStudio/lectureStudio/compare/v6.1.920..v6.1.x) - 2024-02-18

### Other

- Linux deb package support

## [6.1.920](https://github.com/lectureStudio/lectureStudio/compare/v6.0.912..v6.1.920) - 2024-02-16

### Other

- Fixed library ([#870](https://github.com/lectureStudio/lectureStudio/issues/870))
- Updated new version metadata
- Changed line breaks for macOS scripts

## [6.0.912](https://github.com/lectureStudio/lectureStudio/compare/v5.3.725..v6.0.912) - 2023-12-21

### Presenter

- Fixed crash with only one display connected
- Increased screen-share bitrate
- Fixed stopping minimized shared window
- Remove captured actions for closed documents
- Fixed transmitting page actions of closed documents
- Reduced screen-document size
- Added window focus listener and pause screen-sharing on lost ([#685](https://github.com/lectureStudio/lectureStudio/issues/685))
- Create new screen-slide when presenter gained ([#685](https://github.com/lectureStudio/lectureStudio/issues/685))
- Close screen-document when the screen source has been closed
- Show notification if the screen source is no longer available
- Copy annotations of replaced screen-document
- Fixed document replacement ([#696](https://github.com/lectureStudio/lectureStudio/issues/696))
- Send document checksum on new document
- Use AudioProcessingSettings for microphone level adjustment
- Added ScreenRecorderService to write shared screen/window to video files
- Fixed screen-share recording
- Fixed starting stream with closed ([#727](https://github.com/lectureStudio/lectureStudio/issues/727))
- Removed legend from QuizDocument charts
- Stop streaming do not close documents related to a screen source
- Fixed ScreenRecorderService audio/video synchronisation
- ScreenRecorderService reduced memory footprint
- Fixed restarting screen-share after streaming ([#731](https://github.com/lectureStudio/lectureStudio/issues/731))
- Close recorded screen-share video file when recording ([#732](https://github.com/lectureStudio/lectureStudio/issues/732))
- Improved quiz document rendering (margin) ([#738](https://github.com/lectureStudio/lectureStudio/issues/738))
- Show quizzes for all opened documents, no matter which one is ([#539](https://github.com/lectureStudio/lectureStudio/issues/539))
- Show specific error message when a quiz file was not ([#592](https://github.com/lectureStudio/lectureStudio/issues/592))
- Fixed transmitting quiz documents having multi-page ([#741](https://github.com/lectureStudio/lectureStudio/issues/741))
- Fixed audio input level visualization due to updated webrtc lib
- Repaired and refactored tests to be partially running again. ([#751](https://github.com/lectureStudio/lectureStudio/issues/751))
- Improved loading quiz resources (images) ([#729](https://github.com/lectureStudio/lectureStudio/issues/729))
- Fixed 'CoInitializeEx() failed' on calling Desktop.getDesktop().open on post JDK 14 systems
- Fixed regression with streamed ([#762](https://github.com/lectureStudio/lectureStudio/issues/762))
- Fixed spaces in file paths for quiz ([#729](https://github.com/lectureStudio/lectureStudio/issues/729))
- Using mandatory client-id for web-api communication
- Do not set quiz resource urls
- Add collective state observation when starting a stream
- Minor stream event related fixes
- Fixed starting chat before stream
- Improved stream preview timing
- Fixed unnecessary notification dialog when starting chat together with the stream
- Fixed stream state handling on stop event
- Fixed master volume number format ([#841](https://github.com/lectureStudio/lectureStudio/issues/841))
- Display audio device connection events with popups
- Handle disconnected microphone during the ([#840](https://github.com/lectureStudio/lectureStudio/issues/840))
- Persist JCEF cache to the file system
- Catch errors when no audio device is connected

### Presenter API

- Create quizzes faster with new button and accelerators
- Introduced ScreenShareContext to dynamically adjust screen-share settings
- Use more performant VideoFrame for desktop capture
- Minor screen share UI improvements
- Disable collapsing of document ([#697](https://github.com/lectureStudio/lectureStudio/issues/697))
- Added annotation select-move button to toolbar
- Added ScreenCaptureService to enable screen-share without a stream
- Fixed focus selected screen source
- Load pending participants when stream started
- Fixed text-highlight color buttons disabled with rubber-([#723](https://github.com/lectureStudio/lectureStudio/issues/723))
- Restrict document tab name to max. 32 characters
- (de)activate audio noise suppression in audio settings
- Fixed autostart of audio capture ([#742](https://github.com/lectureStudio/lectureStudio/issues/742))
- Fixed HTMLEditor scrolling
- Improved UI when handling stream reconnects
- Disable preview-panel when the slide-view has input focus
- Added (general) option to disable user input in slide-preview
- Added missing test case
- Added toolbar button to disable user input in slide-preview
- Set toolbar icon size to 24px
- Added a page selection delay for the ThumbPanel
- Added an option to change the position of the slide preview
- Refactored the MessagePanel and subclasses
- Increased SplitPane divider width to 10px
- Select and show message tab content if a new message is received
- Fixed multi-line message box size when auto showing message tab
- Set progress bar to indeterminate while saving ([#750](https://github.com/lectureStudio/lectureStudio/issues/750))
- Handle missing min/max values for numeric quiz ([#765](https://github.com/lectureStudio/lectureStudio/issues/765))
- Handle default min/max values for numeric quiz ([#765](https://github.com/lectureStudio/lectureStudio/issues/765))
- Restrict quiz max option count to ([#589](https://github.com/lectureStudio/lectureStudio/issues/589))
- Improved quiz change state for the quiz ([#61](https://github.com/lectureStudio/lectureStudio/issues/61))
- Updated web api uri and attach opaque id to Janus handles
- Refactored course participants count display
- Fixed quiz missing max ([#772](https://github.com/lectureStudio/lectureStudio/issues/772))
- Improved quiz min/max options
- Resizeable stream preview
- Added option to disable pen input and use mouse instead
- Fixed participants list in separate ([#776](https://github.com/lectureStudio/lectureStudio/issues/776))
- Show dialog to remind to start the recording
- Automatically start recording when starting a stream
- Manage muted microphone and recording ([#836](https://github.com/lectureStudio/lectureStudio/issues/836))
- Manage muted microphone and pause recording when starting a ([#836](https://github.com/lectureStudio/lectureStudio/issues/836))
- Fixed toolbar button ([#851](https://github.com/lectureStudio/lectureStudio/issues/851))

### Editor

- Zip vector export
- Fixed page selection after document replacement  ([#736](https://github.com/lectureStudio/lectureStudio/issues/736))
- Added new test to test the RecordingFileService in isolation ([#759](https://github.com/lectureStudio/lectureStudio/issues/759))
- Fixed default audio device ([#508](https://github.com/lectureStudio/lectureStudio/issues/508))
- Fixed seeking after playing back to the ([#805](https://github.com/lectureStudio/lectureStudio/issues/805))
- Fixed opening multiple ([#822](https://github.com/lectureStudio/lectureStudio/issues/822))
- Fixed regression after selecting a recording
- Fixed rubber event rendering in timeline and ([#807](https://github.com/lectureStudio/lectureStudio/issues/807))
- Fixed text tool ([#847](https://github.com/lectureStudio/lectureStudio/issues/847))
- Set time marker to the cut position after removing a ([#861](https://github.com/lectureStudio/lectureStudio/issues/861))
- Fixed removal of unused ([#827](https://github.com/lectureStudio/lectureStudio/issues/827))
- Improved removal of unused ([#827](https://github.com/lectureStudio/lectureStudio/issues/827))
- Fixed removal of unused zoom ([#860](https://github.com/lectureStudio/lectureStudio/issues/860))
- Added ability to remove composite actions
- Fixed removal of the zoom-out ([#860](https://github.com/lectureStudio/lectureStudio/issues/860))
- Fixed adding annotations in zoomed ([#854](https://github.com/lectureStudio/lectureStudio/issues/854))
- Increased margin error when removing an overlapped ([#864](https://github.com/lectureStudio/lectureStudio/issues/864))
- Ensure the file extension exists when saving files on ([#812](https://github.com/lectureStudio/lectureStudio/issues/812))

### Editor API

- Improved export dialog field ([#737](https://github.com/lectureStudio/lectureStudio/issues/737))
- Minor improvements for the page replacement ([#761](https://github.com/lectureStudio/lectureStudio/issues/761))
- Minor improvements for the document split ([#758](https://github.com/lectureStudio/lectureStudio/issues/758))
- Fixed crash on playback of denoised ([#745](https://github.com/lectureStudio/lectureStudio/issues/745))
- Small fix for issue when focusing collapsable menus
- Fixes the event list by showing the laserpointer again and showing the icon and name for the rubber tool. Bugs ([#807](https://github.com/lectureStudio/lectureStudio/issues/807)) ([#806](https://github.com/lectureStudio/lectureStudio/issues/806)) ([#809](https://github.com/lectureStudio/lectureStudio/issues/809))
- Enable playback with space key after opening a recording
- Fixed UI freeze after chaning ([#845](https://github.com/lectureStudio/lectureStudio/issues/845)) ([#846](https://github.com/lectureStudio/lectureStudio/issues/846)) ([#850](https://github.com/lectureStudio/lectureStudio/issues/850))
- Fixed event table height by filling the empty space
- Avoid event table ([#826](https://github.com/lectureStudio/lectureStudio/issues/826))
- Fixed event table ([#826](https://github.com/lectureStudio/lectureStudio/issues/826))

### Core

- Fixed CVE-2022-42003 (Uncontrolled Resource Consumption in Jackson-databind)
- Render screen document with white background and border around the frame
- Fixed screen document image layout
- Replaced documents maintain the page presentation state
- Removed unused SRP code
- Use mutex with all internal MuPDF document structure
- Removed unnecessary concurrent executor from MuPDFDocument
- Fixed minor non-existent setting with Latex tool
- Fixed TextTool sending ShapePaintEvent
- Fixed TextShape moving by delta
- Do not copy volatile shapes like pointer or zoom-rect when cloning ([#704](https://github.com/lectureStudio/lectureStudio/issues/704))
- Fixed PDFBoxRenderer (white) background rendering
- Fixed handling replaced documents for presentation ([#744](https://github.com/lectureStudio/lectureStudio/issues/744))
- Fixed pie chart text ([#628](https://github.com/lectureStudio/lectureStudio/issues/628))
- Register recorded changes in any ([#292](https://github.com/lectureStudio/lectureStudio/issues/292))
- Introduced audio device change handler and listener

### Media

- Added hardware accelerated codecs for selection
- Log FFmpeg information when rendering export video
- Fixed FileEventExecutor for sleeping too long after seeking between ([#656](https://github.com/lectureStudio/lectureStudio/issues/656))
- Improved default (systems) audio device retrieval
- Fixed audio waveform ([#553](https://github.com/lectureStudio/lectureStudio/issues/553))
- Dispatch device connection events with WebRTC audio provider

### Web API

- Fixed setting video stream bitrate
- Fixed muting remote speech participant
- Observe and handle screen-source 'end' event
- Set media stream encoding constraints at stream runtime
- Push stream media state to remote course state
- Set participants name when starting a stream publisher
- Fixed GitHubService to check for generic repos
- Added getParticipants() to StreamRestClient
- Better naming for local/remote video frames + capture local camera video event
- Revert (unknown host) for maven central repository
- Removed custom TrustManager from WebSocketStompTransport
- Stopping STOMP transport gracefully
- Added WebSocketStompTransport reconnection behaviour
- Extended Janus message handling
- Refined reconnection procedure for individual channels
- Notify course participants that the stream was interrupted
- Simplified usage of ClientFailover
- Fixed issues with janus states after reconnection
- Refactored media state change event propagation
- Introduced client-api to the StreamRestClient and make use of lombok
- Moved module to project root
- Start message transport before the stream
- Fixed direct message parsing
- Reverted usage of client-id for message transport

### JavaFX

- Moved FxNewVersionView from editor-fx
- Improved MediaTracks scrolling in zoomed ([#583](https://github.com/lectureStudio/lectureStudio/issues/583))
- Fixed clear event marker ([#815](https://github.com/lectureStudio/lectureStudio/issues/815))
- Changed event marker color in the ([#815](https://github.com/lectureStudio/lectureStudio/issues/815))

### Swing

- Generate accelerator string for buttons
- Fixed PresentationWindow slide update
- Fixed updating PresentationWindow after screen-share pause/stop
- Fixed text-box location
- Removed textbox frame header and set frame color to yellow
- Fixed calculating real text size
- Extended clipping rectangle for TextShape
- Call slide-view centering from the AWT event dispatching ([#721](https://github.com/lectureStudio/lectureStudio/issues/721))
- Change view context from the AWT event dispatching ([#721](https://github.com/lectureStudio/lectureStudio/issues/721))
- Clear ParticipantList
- Fixed async state behaviour between focus and click events for screen sharing
- Fixed quiz page selection constraints for more than two option ([#589](https://github.com/lectureStudio/lectureStudio/issues/589))
- Fixed selecting first quiz page in the thumbnail panel
- Fixed negative numbers for ([#476](https://github.com/lectureStudio/lectureStudio/issues/476))
- Fixed SlideView rendering after addition/removal of ([#816](https://github.com/lectureStudio/lectureStudio/issues/816))
- Fixed external display slide scaling
- Improved notification popup layout and appearance
- Persist PdfDocumentRenderer fixed saving ([#834](https://github.com/lectureStudio/lectureStudio/issues/834))
- Fixed text bounds ([#852](https://github.com/lectureStudio/lectureStudio/issues/852))

### Other

- Fixed missing snapshot artifact dev.onvoid.webrtc:webrtc-([#653](https://github.com/lectureStudio/lectureStudio/issues/653))
- *(deps)* Bump ua-parser-js ([#730](https://github.com/lectureStudio/lectureStudio/issues/730))
- Updated Windows version to 5.1.2
- Dropped unused web-frontend module
- *(deps)* Bump http-cache-semantics and @angular/cli ([#734](https://github.com/lectureStudio/lectureStudio/issues/734))
- Updated dependencies
- Updated packages
- Updated project dependencies
- Use UUID for speech events
- *(deps-dev)* Bump webpack in /lect-player-web/src/main/frontend ([#740](https://github.com/lectureStudio/lectureStudio/issues/740))
- Updated module dependencies
- Minor refactorings
- Set multi-release to 17
- Remember recently used folder for image ([#65](https://github.com/lectureStudio/lectureStudio/issues/65))
- Added wrapping to the ([#754](https://github.com/lectureStudio/lectureStudio/issues/754))
- Hinzufügen von Annotationen im Editor ([#799](https://github.com/lectureStudio/lectureStudio/issues/799))
- Audio Loudness Normaliaztion and QoL improvements ([#804](https://github.com/lectureStudio/lectureStudio/issues/804))
- Incremented version to 6.0
- *(deps-dev)* Bump postcss in /lect-player-web/src/main/frontend ([#825](https://github.com/lectureStudio/lectureStudio/issues/825))
- Set webrtc-java version to 0.8.([#821](https://github.com/lectureStudio/lectureStudio/issues/821))
- *(deps-dev)* Bump @babel/traverse ([#828](https://github.com/lectureStudio/lectureStudio/issues/828))
- Updated project poms

## [5.3.725](https://github.com/lectureStudio/lectureStudio/compare/v5.3.720..v5.3.725) - 2022-10-13

### Presenter

- Fixed stopping running screen-share

### Web API

- WebSocketStompClient set explicitly default heartbeat

### Other

- Version 5.3.x bump

## [5.3.720](https://github.com/lectureStudio/lectureStudio/compare/v5.1.660..v5.3.720) - 2022-10-12

### Presenter

- Validate normalized document template bounds

### Presenter API

- Removed screen-share toggle functionality, i.e. prepare multiple screen sources
- Allow opening multiple screen source documents

### Editor

- Moved edit actions from core module
- Fixed import of recordings with a different audio format

### Editor API

- Check the inserted audio format

### Core

- Fixed bad screen-dump quality in ScreenDocument

### Other

- *(deps)* Bump shell-quote ([#651](https://github.com/lectureStudio/lectureStudio/issues/651))
- *(deps)* Bump jsoup from 1.14.3 to 1.15.3 in /lect-presenter-api ([#646](https://github.com/lectureStudio/lectureStudio/issues/646))

## [5.1.660](https://github.com/lectureStudio/lectureStudio/compare/v5.1.555..v5.1.660) - 2022-06-15

### Presenter

- Write recorded audio into a mixed audio stream file
- Minor code formatting
- Do not send local recording pause and resume events to peers
- Added more margin around quiz ([#516](https://github.com/lectureStudio/lectureStudio/issues/516))
- Save received text messages in a PDF ([#513](https://github.com/lectureStudio/lectureStudio/issues/513))
- Removed axis ticks line for quiz ([#528](https://github.com/lectureStudio/lectureStudio/issues/528))
- Sorting of numeric ([#523](https://github.com/lectureStudio/lectureStudio/issues/523))
- New quiz document ([#520](https://github.com/lectureStudio/lectureStudio/issues/520))
- HTML to PDF document implementation
- MessageDocument ([#468](https://github.com/lectureStudio/lectureStudio/issues/468))
- Improved HTML presentation with empty blocks
- Fixed quiz question line-([#533](https://github.com/lectureStudio/lectureStudio/issues/533))
- Fixed quiz document ([#533](https://github.com/lectureStudio/lectureStudio/issues/533)) ([#535](https://github.com/lectureStudio/lectureStudio/issues/535))
- Fixed SoundSettingsPresenterTest
- Fixed duplicate quiz page ([#518](https://github.com/lectureStudio/lectureStudio/issues/518)) ([#541](https://github.com/lectureStudio/lectureStudio/issues/541))
- Removed left padding for quiz options in QuizDocument
- QuizDocument separate stylesheet file
- MessageDocument separate stylesheet file
- HTML to PDF resource file loading
- New quiz resource handling - start-quiz v2 api
- Removed deprecated code
- Add template loading for Quiz- and MessageDocument
- Bind template config bounds to the view
- Open whiteboard with ([#558](https://github.com/lectureStudio/lectureStudio/issues/558))
- Fixed template content bounds
- Set same font size for template documents
- Upload whiteboard templates when streaming
- Fixed StartRecordingPresenter close()
- Allow to save a quiz without options
- Validate audio capture and playback devices prior ([#556](https://github.com/lectureStudio/lectureStudio/issues/556))
- Fixed whiteboard templates used for ([#558](https://github.com/lectureStudio/lectureStudio/issues/558))
- Zero margin and padding for the root of html documents
- Use UUID as document file name for streams
- Changed quiz chart ([#545](https://github.com/lectureStudio/lectureStudio/issues/545))
- Moved to a better and simpler quiz storage ([#565](https://github.com/lectureStudio/lectureStudio/issues/565))

### Presenter API

- Removed unused quiz-related code
- Make use of the audio mixing configuration
- Updated quiz editor icons
- Set audio playback volume and play a test sound via ([#495](https://github.com/lectureStudio/lectureStudio/issues/495))
- Removed enter key action for options in CreateQuizPresenter
- Removed unused code for page uris and embedded quizzes
- Added DocumentTemplateSettingsView and TemplateConfiguration
- Added content bounds to document templates
- Increased preview size for template settings
- Added button to toolbar to create slides for messages from the audience
- Show audio device dialog before starting a ([#556](https://github.com/lectureStudio/lectureStudio/issues/556))
- Removed unsupported options from general ([#11](https://github.com/lectureStudio/lectureStudio/issues/11))
- Improved DocumentPreview
- Use normalized content bounds for templates

### Editor

- Fixed browse video export path when old path is invalid
- Increased max zoom level for the wave-form
- Synchronized page ([#538](https://github.com/lectureStudio/lectureStudio/issues/538))
- Async media track loading
- Fixed temporary page selection when document has changed
- Fixed UI freeze after removing a specific tool ([#551](https://github.com/lectureStudio/lectureStudio/issues/551))

### Editor API

- Fixed page replacement ([#252](https://github.com/lectureStudio/lectureStudio/issues/252))
- Enable deletion of single actions without markers
- Make use of input validators
- Fixed video settings path validation

### Core

- Introduced AudioMixer to mix multiple audio streams into one
- Audio mixing configuration
- Do not paint over previously marked ([#517](https://github.com/lectureStudio/lectureStudio/issues/517))
- Added DoubleIntegerConverter for bean properies
- Fixed padding calculation when removing portions in the audio stream
- Fixed removing portions in the audio ([#536](https://github.com/lectureStudio/lectureStudio/issues/536)) ([#538](https://github.com/lectureStudio/lectureStudio/issues/538))
- Improved thread safety for recorded objects
- Set document title on parsing, if it is missing
- Minor code formatting
- Removed unused code for page uris
- Added TemplateDocument
- Added MuPDF page importing and removed whiteboard specific rendering
- Open whiteboard with ([#558](https://github.com/lectureStudio/lectureStudio/issues/558))
- MuPDFDocument permissive page grafting
- Fixed page removal with ([#562](https://github.com/lectureStudio/lectureStudio/issues/562))
- Generic whiteboard page number re-alignment

### Media

- Synchronized methods in RecordingPlayer
- AudioTrack setData async
- Faster waveform generation
- Avoid toolController.selectPage(0) on page reset
- Fixed page selection in preloading phase

### Web API

- Provide access to remote peer audio streams
- Set playback volume for Janus peer-connections
- Added WebSocketClientHandler with heartbeat timer
- Use page's page number for StreamPageAction

### JavaFX

- Always render waveform in the UI thread
- Position sliders after MediaTracks initialized
- Improved timeline presentation
- Always render EventTimeline in the UI thread
- Fixed panning of the zoomed ([#35](https://github.com/lectureStudio/lectureStudio/issues/35))
- Text-input validators
- Resilient PathValidator
- Java 14 switch expressions

### Swing

- Added DocumentPreview
- Cleaned up SlideRenderer
- Added Resizable and ResizableBorder
- DocumentPreview content bounds handling
- Minor Resizable fixes
- Fixed DocumentPreview size calculation
- ThumbPanel fixed page selection with frequently changed ([#562](https://github.com/lectureStudio/lectureStudio/issues/562))

### Other

- Fixed HTML code block and toolbar icon sizes
- Set the maximum heap size to 4GB
- Set maven compiler version to 14

## [5.1.555](https://github.com/lectureStudio/lectureStudio/compare/v5.1.544..v5.1.555) - 2022-04-04

### Presenter

- Fixed streaming without any connected ([#488](https://github.com/lectureStudio/lectureStudio/issues/488))
- Preserve compatibility to old configs

### Presenter API

- Configurable streaming server name
- Removed unused code
- Config validation with mandatory properties
- Removed save quiz results menu item

### Editor API

- Query audio devices in main thread

### Core

- Fixed package version retrieval on ([#489](https://github.com/lectureStudio/lectureStudio/issues/489))

### Web API

- Updated keystore with new key-pair

### Swing

- Fixed draggable toolbar button visibility on linux

### Other

- Fixed pre-loader app version number on ([#489](https://github.com/lectureStudio/lectureStudio/issues/489))

## [5.1.544](https://github.com/lectureStudio/lectureStudio/compare/v5.0.438..v5.1.544) - 2022-03-28

### Presenter

- Send start event from the event recorder
- Improved camera profile selection based on the supported formats
- Fixed cyclic settings dependency in camera settings
- Changed quiz options indentation in the slides
- Improved quiz slide rendering
- Changed quiz chart color palette
- Draw chart questions in separate stream
- Fixed quiz option wrong background ([#463](https://github.com/lectureStudio/lectureStudio/issues/463))
- Fixed multi-line message rendering on PDF slides
- Added URL parsing for shown messages on slides
- Improved message document and slide creation
- Fixed saving edited quiz twice
- Fixed reloading SelectQuizView after editing
- Do not clear quiz options on new quiz type
- Improved chart color palette
- Delete selected quiz in any set
- Save and load split-pane divider ([#471](https://github.com/lectureStudio/lectureStudio/issues/471))
- Fixed showing new quiz slide when overwriting an old ([#472](https://github.com/lectureStudio/lectureStudio/issues/472))
- Make use of ClientJsonMapper by quiz client
- WebService create new message transport
- Shorten quiz option text on chart slides
- Allow option text to be two lines long below the charts
- Show only pages of selected documents
- Fixed message document bounds

### Presenter API

- Improved document selection tab behaviour
- Fixed control keys with input fields when creating a quiz
- Toolbar stream buttons have green background when active
- Quit accepted and pending speech requests
- Changed color of camera and microphone icons in off-state
- Minor UI improvements
- Fixed camera preview in StartStreamView
- Removal of peer-views
- Removed share quiz button from the quiz-doc ([#411](https://github.com/lectureStudio/lectureStudio/issues/411))
- Added sponsors to the info view
- Minor changes to about view
- Added icons to remote message boxes
- Do not remove remote text messages when the service stops
- Added icons to speech request ([#457](https://github.com/lectureStudio/lectureStudio/issues/457))
- Fixed quiz selection view ([#446](https://github.com/lectureStudio/lectureStudio/issues/446))
- Create quiz from the quiz selection ([#473](https://github.com/lectureStudio/lectureStudio/issues/473))
- Fixed split-pane divider position calculation
- Fixed minor quiz rendering issues
- Remove unused slide-view code

### Editor

- Use file name instead of recorded document name when saving a ([#433](https://github.com/lectureStudio/lectureStudio/issues/433))
- Fixed rendering of overwritten ([#420](https://github.com/lectureStudio/lectureStudio/issues/420)) ([#397](https://github.com/lectureStudio/lectureStudio/issues/397))

### Editor API

- Changed remove page ([#410](https://github.com/lectureStudio/lectureStudio/issues/410))
- Added sponsors to the info view

### Core

- Fixed removing the end of a laser pointer ([#394](https://github.com/lectureStudio/lectureStudio/issues/394))
- Added sponsor resources
- Improved pdf chart rendering performance
- Allow to reload the internal backing document structure
- Updated jackson version to 2.13.2
- Fixed alpha blending when rendering ([#477](https://github.com/lectureStudio/lectureStudio/issues/477)) ([#478](https://github.com/lectureStudio/lectureStudio/issues/478))
- Introduced UUID for ([#479](https://github.com/lectureStudio/lectureStudio/issues/479))
- Fixed recording quiz question slides n-([#479](https://github.com/lectureStudio/lectureStudio/issues/479))
- Fixed text-selection point to rectangle collision ([#486](https://github.com/lectureStudio/lectureStudio/issues/486))

### Media

- Fixed camera being active when closed
- Fixed NPE caused by device-module in WebRtcAudioPlayer
- Fixed selecting non-existent audio device for ([#475](https://github.com/lectureStudio/lectureStudio/issues/475))

### Web API

- Manage speech participants with request ids
- Improved janus peer handling
- Strict participant speech handling
- Fixed accepting speech ([#399](https://github.com/lectureStudio/lectureStudio/issues/399))
- Janus api improvements
- Send stream recorded state action
- Parse stream recorded state action
- Get recorded state
- Added CourseFeatureResponse
- Rest api set stream recorded state
- Make use of the rest api to set stream recorded state
- Minor code clean-up
- Add json sub-types to FilterRule
- MinMaxRule default constructor and setters
- Resteasy jackson client provider
- Drop persistence annotations from quiz

### JavaFX

- Fixed updating slide bounds on new ([#481](https://github.com/lectureStudio/lectureStudio/issues/481))

### Swing

- Improved PeerView layout and button states
- Improved camera image quality on receiving side
- Synchronized methods in CameraPanel
- Fixed tabbed-pane tab orientation for settings panes
- Fixed MessageView text-area ([#468](https://github.com/lectureStudio/lectureStudio/issues/468))
- Fixed updating slide bounds on new ([#477](https://github.com/lectureStudio/lectureStudio/issues/477))
- Fixed page preview size in combination with scroll-bar
- Removed buffer.retain() while converting video frames

### Other

- Exclude webrtc-java from packaging
- Exclude presenter-fx from build
- *(deps)* Bump tar ([#415](https://github.com/lectureStudio/lectureStudio/issues/415))
- *(deps)* Bump log4js ([#414](https://github.com/lectureStudio/lectureStudio/issues/414))
- *(deps)* Bump follow-redirects ([#413](https://github.com/lectureStudio/lectureStudio/issues/413))
- *(deps)* Bump follow-redirects in /lect-player-web/src/main/frontend ([#422](https://github.com/lectureStudio/lectureStudio/issues/422))
- *(deps-dev)* Bump karma ([#425](https://github.com/lectureStudio/lectureStudio/issues/425))
- *(deps)* Bump follow-redirects ([#424](https://github.com/lectureStudio/lectureStudio/issues/424))
- *(deps-dev)* Bump node-sass in /lect-player-web/src/main/frontend ([#423](https://github.com/lectureStudio/lectureStudio/issues/423))
- Removed unused modules from build
- Improved build
- Removed unused code and dependencies
- *(deps-dev)* Bump karma ([#455](https://github.com/lectureStudio/lectureStudio/issues/455))
- *(deps)* Bump url-parse ([#453](https://github.com/lectureStudio/lectureStudio/issues/453))
- Updated dependency versions
- Increment package version to 5.1.n

### Documentation

- Added sponsors to README

## [5.0.438](https://github.com/lectureStudio/lectureStudio/compare/v4.4.288..v5.0.438) - 2022-01-17

### Presenter

- Remove closed quiz document
- MessageTransport implementation
- Propagate audio device selection to WebRtcStreamService
- Adopted to the new message transport api
- Improved consuming quiz messages
- Fixed usage of removed advanced settings option
- Initial config enable by default to save documents on exit
- Camera selection observing allows to change the camera for WebRTC streams at runtime
- Catch StreamMediaException and show UI feedback
- New StartStreamView
- Default camera bitrate set to 200 ([#146](https://github.com/lectureStudio/lectureStudio/issues/146))
- Tighter cohesion for WebRTC services
- Fixed adjusting the recording ([#288](https://github.com/lectureStudio/lectureStudio/issues/288))
- Remove recorded actions for document that has been ([#294](https://github.com/lectureStudio/lectureStudio/issues/294))
- Fixed getting values from stream.properties in StreamSettingsPresenter
- Quiz stop and share interoperability
- Fixed StartPresenter handling
- Fixed recording pages of a document that has replaced its internal page ([#113](https://github.com/lectureStudio/lectureStudio/issues/113))
- Limit starting of a stream/quiz/messenger to only one course at a ([#309](https://github.com/lectureStudio/lectureStudio/issues/309))
- Transmit re-started quiz document only in initial ([#313](https://github.com/lectureStudio/lectureStudio/issues/313))
- Fixed restart of a quiz to open a new document tab, if ([#315](https://github.com/lectureStudio/lectureStudio/issues/315))
- Copy quiz-page annotations upon document re-creation when new answers ([#316](https://github.com/lectureStudio/lectureStudio/issues/316))
- Dropped unnecessary speech classes
- Fixed empty course list in StartStreamPresenter
- CameraSettingsPresenter: do not interfere with the running stream
- Fix camera view when a stream is active without an active ([#362](https://github.com/lectureStudio/lectureStudio/issues/362))

### Presenter API

- Renamed microphone settings into sound settings. Added option to select an audio playback device in the sound settings.
- Simplified settings
- Removed advanced settings menu item and use advanced settings by default
- Stream settings web-api title
- Set log level to 'error'
- Fixed stylus in fullscreen ([#249](https://github.com/lectureStudio/lectureStudio/issues/249))
- Resilient camera selection with status message on error
- Show camera in StartStreamView
- Fixed StartStreamView combo boxes
- Messenger/quiz StartCourseFeatureView implementation
- Fixed document-tab focus which blocked shortcuts
- Test sound in StartStreamView
- Truncate too long combo-box item strings
- Fixed initial window size (non-maximized) for scaled desktops
- Use new ThumbnailPanel in the SlidesView
- Share quiz functionality
- Moved toolbar creation to xml
- Prev/next slide buttons for the ([#293](https://github.com/lectureStudio/lectureStudio/issues/293))
- Toolbar button for quiz-([#291](https://github.com/lectureStudio/lectureStudio/issues/291))
- Consistent button naming to stop a ([#322](https://github.com/lectureStudio/lectureStudio/issues/322))
- Share quiz button handling in conjunction with the stream ([#323](https://github.com/lectureStudio/lectureStudio/issues/323))
- Fixed text-field input conflict with ([#122](https://github.com/lectureStudio/lectureStudio/issues/122))
- Fixed control key when considering shortcuts with focused text-fields
- Non-blocking UI when changing camera
- Remove unused tabs at the bottom of the slide ([#364](https://github.com/lectureStudio/lectureStudio/issues/364))
- Removed latex tool button from ([#364](https://github.com/lectureStudio/lectureStudio/issues/364))
- Set default customize toolbar buttons

### Editor

- Fixed document loading for video ([#367](https://github.com/lectureStudio/lectureStudio/issues/367))

### Editor API

- Unified control sizes
- Hide (temporary) event table from the main ([#348](https://github.com/lectureStudio/lectureStudio/issues/348))
- Remove two-pass encoding ([#365](https://github.com/lectureStudio/lectureStudio/issues/365))
- Fixed maximized to un-maximized layout
- Fixed play button size
- Ignore jboss log messages

### Core

- Minor text-selection fix
- Use more descriptive property names in the AudioConfiguration
- Do not close a removed document from the DocumentService
- Fixed connected screen bounds
- Fixed screen location ([#265](https://github.com/lectureStudio/lectureStudio/issues/265))
- Improved convenience methods for notifications in the Presenter class
- Moved convenience state methods from ExecutableBase to the Executable interface
- Fixed document hashCode() and equals() for empty title
- Fixed close document and saving this document ([#297](https://github.com/lectureStudio/lectureStudio/issues/297))
- Fix saving documents on app close when multiple documents have been shown without ([#281](https://github.com/lectureStudio/lectureStudio/issues/281))
- Fault-tolerant ToolController in case a non-existent annotation has to be ([#346](https://github.com/lectureStudio/lectureStudio/issues/346))
- Fixed cutting off tool begin ([#357](https://github.com/lectureStudio/lectureStudio/issues/357))
- Improved cutting off sections tool ([#357](https://github.com/lectureStudio/lectureStudio/issues/357))
- Fixed cutting off the end of a zoom annotation

### Media

- Fixed audio playback device changing when the AudioPlayer has been ([#289](https://github.com/lectureStudio/lectureStudio/issues/289))
- Fix memory access violation (buffer overflow) for scaled camera frames
- Select the best suited camera format for the provided format's aspect ratio in the preview

### Web API

- Bump netty-codec from 4.1.59.Final to 4.1.68.Final
- Fire connected state dependent on the ICE state, add more debugging info
- Take selected audio playback device into account for WebRTC
- Non-transient InputFieldFilter for quiz
- Fixed minor SlideView NPE with back image
- MessageTransport implementation
- JanusPeerConnection change capture and playback device at runtime
- Added more audio processing options to JanusPeerConnection
- MessageTransport modifications
- Shared JanusPeerConnectionFactory for all peer connections
- Send start event with course id when starting a course feature ([#266](https://github.com/lectureStudio/lectureStudio/issues/266))
- Janus state and peer connection error handling
- Additional JanusHandler exception handling
- Refactored JanusPeerConnection for better handling of cameras
- Removed unused stream-related code
- Use ApiKeyFilter for access token authentications
- Improved recorded action handling within StreamWebSocketClient
- Handle successive acceptance of speech ([#285](https://github.com/lectureStudio/lectureStudio/issues/285))
- Improved concurrent Janus states being permissive handling each other messages
- Select the best suited camera format for the provided format's aspect ratio
- Change camera and camera format for the running streaming session
- Provide event-bus to JanusHandler to handle new incoming messages
- CourseParticipantMessage extends WebMessage
- Fix web messages JSON mapping
- Fix peer-connection starting camera source on new capture ([#362](https://github.com/lectureStudio/lectureStudio/issues/362))
- Janus peer-connection logging with log4j

### Swing

- Fixed DisplayPanel screen bounds
- Fixed screen button color in ([#257](https://github.com/lectureStudio/lectureStudio/issues/257))
- Set status message on CameraPanel
- UI scale fixes mostly for unix based systems
- Fixed toolbar icon size for scaled desktops
- Generic ThumbnailPanel to which buttons can be added
- ThumbnailPanel for quiz and whiteboard documents
- Block quiz-slide selection until the quiz is ([#316](https://github.com/lectureStudio/lectureStudio/issues/316))
- Removed unused classes
- CameraPanel handle non-existent camera
- CameraPanel repaint as soon as a camera format has been set
- Fixed selected button state for the customizable ([#368](https://github.com/lectureStudio/lectureStudio/issues/368))
- Fixed CameraPanel size on Unix systems

### Other

- Unified app packaging for all systems
- Depend on stable stylus and webrtc-java libraries
- *(deps)* Bump jackson-databind from 2.9.10.7 to 2.9.10.8 in /lect-core
- Delete stylus library
- Skip tests as it is deprecated by now
- Removed lectPlayer shortcut creation
- Imported intermediate Let’s Encrypt R3 certificate
- *(deps)* Bump netty-codec-http in /lect-web-parent/lect-web-api ([#301](https://github.com/lectureStudio/lectureStudio/issues/301))
- *(deps)* Bump log4j-core in /lect-web-parent/lect-web-api ([#306](https://github.com/lectureStudio/lectureStudio/issues/306))
- *(deps)* Bump log4j-core from 2.14.0 to 2.15.0 ([#305](https://github.com/lectureStudio/lectureStudio/issues/305))
- *(deps)* Bump log4j-api from 2.14.0 to 2.15.0 ([#304](https://github.com/lectureStudio/lectureStudio/issues/304))
- *(deps)* Bump log4j-api in /lect-web-parent/lect-web-api ([#303](https://github.com/lectureStudio/lectureStudio/issues/303))
- Smaller icon size in the HTML editor
- *(deps)* Bump log4j from 2.15.0 to 2.16.0
- Fixed actions with a shape ([#282](https://github.com/lectureStudio/lectureStudio/issues/282))
- *(deps)* Bump log4j from 2.17.0 to 2.17.1
- Append app version to log files
- *(deps)* Updated to the new webrtc-java version

## [4.4.288](https://github.com/lectureStudio/lectureStudio/compare/v4.4.170..v4.4.288) - 2021-10-03

### Presenter

- Added options for the streaming model
- WebRTC video device selection
- Fixed wrongly auto replaced dictionary ([#123](https://github.com/lectureStudio/lectureStudio/issues/123))
- Respect selected audio/video configuration between multiple stream sessions at runtime
- Respect camera bitrate setting
- CVE-2021-37714
- Course feature services
- Speech message routing
- Improved WebRTC message routing
- Fix relay WebRTC video frames
- Do not open the messenger window, for now
- Removed unused code
- Fix start messenger fail ui state
- Introduced lists for remote event messages, for better message handling
- Process course participant events
- Persist settings for the start-stream-([#233](https://github.com/lectureStudio/lectureStudio/issues/233))
- Fixed saving camera capture ([#143](https://github.com/lectureStudio/lectureStudio/issues/143))
- Record all actions for WebRTC streams

### Presenter API

- Adopted to the new course streaming api
- Fixed modifier keys for text inputs
- Update courses in stream settings
- Improved stream settings view
- Streaming camera toggle over the app menu
- Stream microphone muting
- Tool settings scrollpane
- Choose course when starting stream
- Aggregate messaage-views in the main view
- Fixed button ([#191](https://github.com/lectureStudio/lectureStudio/issues/191))
- Fix display remote video frames in the PeerView
- Remove speech request view when canceled, accepted or rejected
- Unified stream api calls
- Bind peer's ID to the PeerView
- Make use of the new generic message-view
- Move interactive feature indicator model to the PresenterContext
- Add start messenger and microphone option in StartStreamView
- Fix bottom tab pane when messenger or stream are stopped
- Fixed missing interface implementations
- Removed show messenger window UI element (temporary) ([#226](https://github.com/lectureStudio/lectureStudio/issues/226))
- Removed red selection rectangle from camera settings view

### Core

- Extended LectureRecorder
- Added missing StreamStartAction parsing
- PageAction long documentId field
- DocumentAction be less restrictive
- Minor changes
- Revert changes that break stroke rendering
- Extended rubber action with explicit shape handle to delete
- Improved text-selection tool
- Minor text-selection fix

### Web API

- VersionChecker clean-up tag name
- Janus WebSocket client implementation with its messages and state machine
- Disambiguated Janus messages and improved code quality
- Introduced more Janus messages and states
- Improved JanusPeerConnection and states to establish it
- Janus state docs
- Janus WebSocket client docs
- Streaming interface interaction
- Better use of the stream configuration
- Fixed WebRTC data channels, improved Janus states, WebSocket state implementation
- Introduced WebSocketHeaderProvider and BearerTokenHeaderProvider
- Export module packages
- Pack recorded playback actions into stream actions
- Improved handling of WebRtcStreamEventRecorder
- Fixed parsing of StreamPageActionsAction
- Do not upload whiteboard documents
- Be more resilient when creating/joining a Janus video room
- Fixed netty dependencies for Unix based platforms
- Deprecate room id and use course id instead
- Improved initial state handling with multiple opened documents
- Netty version bump
- Use new webrtc-java version
- Proper speech request parsing
- Speech message coding
- Improved Janus state machine to run parallel independent peer connections
- Peer state events, room kick, state improvements
- Separate pub/sub handlers for Janus, which enables a much more dynamic handling of pub/sub streams
- Minor Janus message refactorings
- Fixed wrong camera selection in certain ([#193](https://github.com/lectureStudio/lectureStudio/issues/193))
- Minor internal JanusPeerConnection refactoring
- Improved Janus message parsing, renamed StreamService into StreamProviderService
- Preload stream with an init action and publish with the start action
- Fixed StreamInitAction parsing
- Fixed weak listeners when handling peer connections
- Janus publisher/subscriber fixes and improvements
- Implementation of JanusStateHandlerListener, handle slow-link messages
- Get track mid from the peer connection
- Moderate Janus participants done right
- Receive course participant events
- Introduced StreamSpeechPublishedAction to be sent to passive participants
- Refactored StreamSpeechPublishedAction to be compatible with heterogeneous systems
- Fixed course ([#160](https://github.com/lectureStudio/lectureStudio/issues/160))
- Course roomId is now of type string, remove Janus handler when disconnected

### JavaFX

- Fixed zoned date time conversion

### Swing

- Introduced PeerView component
- Generic message-panel implementation
- Fixed button ([#232](https://github.com/lectureStudio/lectureStudio/issues/232))

### Other

- *(deps)* Bump pdfbox from 2.0.23 to 2.0.24 in /lect-core

## [4.4.170](https://github.com/lectureStudio/lectureStudio/compare/v4.4.96..v4.4.170) - 2021-06-07

### Presenter

- Fixed and improved web service classes
- Avoid excessive use backup writing
- Stop web services on shutdown
- Fixed web service notifications and ([#95](https://github.com/lectureStudio/lectureStudio/issues/95))
- Add more description to the stream settings ([#94](https://github.com/lectureStudio/lectureStudio/issues/94))
- Synchronized recorded document in ([#105](https://github.com/lectureStudio/lectureStudio/issues/105))
- Encapsulate recorded page generation in a single threaded ([#105](https://github.com/lectureStudio/lectureStudio/issues/105)) ([#45](https://github.com/lectureStudio/lectureStudio/issues/45))
- Integration test ([#105](https://github.com/lectureStudio/lectureStudio/issues/105)) ([#45](https://github.com/lectureStudio/lectureStudio/issues/45))
- Fixed quiz document ([#106](https://github.com/lectureStudio/lectureStudio/issues/106))
- Added test PDF slides to ([#105](https://github.com/lectureStudio/lectureStudio/issues/105)) ([#45](https://github.com/lectureStudio/lectureStudio/issues/45))
- Restart document recording when a recording has been ([#47](https://github.com/lectureStudio/lectureStudio/issues/47))
- Suggest recording name to that of the first opened PDF ([#87](https://github.com/lectureStudio/lectureStudio/issues/87))
- Fix default recording save ([#110](https://github.com/lectureStudio/lectureStudio/issues/110))

### Presenter API

- Manage broadcast profiles in a table
- Add ip filter ([#96](https://github.com/lectureStudio/lectureStudio/issues/96))
- Fixed enabled menu items when they shouldn't be enabled

### Editor

- Show document preview in it's final playback state
- Repaint SlidesView only if the current event state has changed
- Pre-render slide previews in video export
- Add user readable vector rendering step description
- Fix document export with the same behaviour as with the presenter-([#82](https://github.com/lectureStudio/lectureStudio/issues/82))
- Fix default recording save ([#110](https://github.com/lectureStudio/lectureStudio/issues/110))

### Core

- Synchronized page import and bumped new dependency versions
- Synchronized stroke path generation
- New version notification implementation
- Fixed CameraFormatConverter
- Introduced DocumentEventExecutor to render a document to it's final playback state
- Extended SyncState with page- and event number
- Fixed importing pages with an explicit lock on the source ([#105](https://github.com/lectureStudio/lectureStudio/issues/105)) ([#45](https://github.com/lectureStudio/lectureStudio/issues/45))
- Fixed audio filter copy when cloning RandomAccessAudioStream
- DocumentRecorder merge pages into document with the same name as the page document name
- DocumentEventExecutor - keep extended state on the ([#82](https://github.com/lectureStudio/lectureStudio/issues/82))
- Fix excessive page/([#82](https://github.com/lectureStudio/lectureStudio/issues/82))
- Use common SaveConfigurationHandler

### Media

- WebRTC webcam driver implementation
- Use the extended SyncState and propagate the current event number

### Web API

- Bump log4j.version from 2.11.0 to 2.14.0
- Generalized dependencies
- GitHub client implementation to retrieve releases
- VersionChecker implementation
- OwnTrustManager implementation
- Fix observable filter ([#99](https://github.com/lectureStudio/lectureStudio/issues/99))
- Set client connect timeout to two seconds
- Increase connection timeout to 8 seconds for web ([#102](https://github.com/lectureStudio/lectureStudio/issues/102))
- Added messages to communicate with the Janus WebRTC server
- Increase connection timeout to 12 ([#96](https://github.com/lectureStudio/lectureStudio/issues/96))

### JavaFX

- Cleaned up SlideViewSkin
- Fixed SlideView rendering when bounds change
- Bump JavaFX version to 16
- Minor view refactoring
- Passive SlideView repaint does not react on page changes any more

### Swing

- Introduced TableProperty to bind properties bidirectionally
- Improved in-place document reloading

### Other

- Updated quarkus version and improved quarkus handling
- Fixed tests
- Added additional modules to link with due to the new broadcaster implementation
- Fast start with pre-compiled code
- Load newly generated key pair for JWT verification from the filesystem
- Include locales into the self-contained runtime
- Added new security modules to the self-contained runtime
- Unregister closed SSE sinks and fetch classroom services through the ([#97](https://github.com/lectureStudio/lectureStudio/issues/97))
- Fix start QuarkusServer with two strategies (embedded and standalone)
- Fixed error message ([#97](https://github.com/lectureStudio/lectureStudio/issues/97))

## [4.4.96](https://github.com/lectureStudio/lectureStudio/compare/v4.4.71..v4.4.96) - 2021-04-30

### Presenter

- Save and re-use the recently used directory when saving slides to ([#43](https://github.com/lectureStudio/lectureStudio/issues/43))
- Do not close SaveDocumentsView after saving a selected ([#48](https://github.com/lectureStudio/lectureStudio/issues/48))
- Decouple PdfDocumentRenderer from DocumentRecorder
- Save configuration when exiting application
- Save recently used path for individual documents in ([#81](https://github.com/lectureStudio/lectureStudio/issues/81))

### Presenter API

- Fixed file menu

### Editor

- Fixed undo imported recording
- Fixed positioning of selection ([#49](https://github.com/lectureStudio/lectureStudio/issues/49)) ([#72](https://github.com/lectureStudio/lectureStudio/issues/72))
- Delete temp directory on exit
- Fixed audio track exclusions for vector ([#70](https://github.com/lectureStudio/lectureStudio/issues/70))
- Fixed setting slider position after page ([#49](https://github.com/lectureStudio/lectureStudio/issues/49))

### Editor API

- Replace page feature
- Save document to PDF feature

### Core

- Fixed padding retrieval in ([#27](https://github.com/lectureStudio/lectureStudio/issues/27))
- Fixed ZoomAction stroke ([#68](https://github.com/lectureStudio/lectureStudio/issues/68))
- Fixed stroke NPE for old recordings
- Introduced DummyEventExecutor
- Fixed missing annotation rendering with exported ([#79](https://github.com/lectureStudio/lectureStudio/issues/79))
- Revert padding calculation, ([#27](https://github.com/lectureStudio/lectureStudio/issues/27)) again
- Add edited duration interval to recording changes

### Media

- FileEventExecutor clean-up

### JavaFX

- Do not render closed documents

### Swing

- Add pageScale option to PdfDocumentRenderer

### Other

- Pass action byte length to the action parser

## 4.4.71 - 2021-04-18

### Presenter

- Fix audio capture ([#25](https://github.com/lectureStudio/lectureStudio/issues/25))
- Fixed document saving with correct order of visited pages across all documents
- Fixed writing zoomed and annotated pages to ([#37](https://github.com/lectureStudio/lectureStudio/issues/37))
- Moved document to PDF rendering into its own class
- Introduced recorded changes indicator to the PresenterContext and make use of the injected DocumentRecorder. Also ([#39](https://github.com/lectureStudio/lectureStudio/issues/39))
- Pass PresenterContext to shutdown handlers

### Presenter API

- Load stylus dependency from public repo

### Editor

- Make use of the RecordingEditManager
- Show page progress when exporting to the vector format
- Refactored export variants
- Fixed cancellation of each export variant and improved SoC for the export
- Load web-player resources from the lect-player-web module
- Fixed jar file content copy operation and skip unnecessary files during file transfer
- Render annotations on preview thumbnails for HTML video export

### Editor API

- Collapse timeline selection implementation
- Enable optional video rendering
- Remove HTML video player ([#15](https://github.com/lectureStudio/lectureStudio/issues/15))
- Fixed missing text translation for event-type-icons

### Core

- Bump jackson-databind from 2.9.10.6 to 2.9.10.7
- Remove duplicate document outline names from the DocumentOutline
- Introduced RecordingEditManager and fixed DynamicInputStream audio filter handling
- Remove line tabulation character from outline titles
- Switch configuration to Java 8 date/time api
- Updated AboutView and introduced default AboutPresenter for all apps
- Fixed saving PDF with wrong annotation location and missing blending
- Updated contributors
- Fixed pointer size in zoomed ([#33](https://github.com/lectureStudio/lectureStudio/issues/33))
- Fixed scaling of pen, pointer and zoom tool
- Purged old unused code
- Introduced DocumentRecorder to record all visited documents and pages
- Improved page recording with DocumentRecorder when switching between multiple documents
- Improved build-date and version handling
- Fixed text action execution for the video renderer
- Cleaned-up Document class, make DocumentRecorder injectable and detached from DocumentService
- Fixed translation file loading in deployed releases
- Introduced file context paths in Configuration and file type description translations
- Added extension skip list to file copy operations

### Media

- Fixed stream reset in OpusAudioFileReader which is essential to work with other file readers
- Load avdev dependency from public repo
- Created AudioFilterControl abstraction layer for media track controls
- Cancelable OpusAudioFileWriter

### JavaFX

- Improved interaction between waveform and media track controls
- Fixed z-order of elements in MediaTracksSkin
- Fixed MediaTrackSelection slider collision
- Fixed media track control handling and overlapping
- Provide HostServices as property bound to the primary stage
- Changed FxUtils to set multiple action handlers to controls

### Swing

- Fixed glitches in SlideView and ThumbPanel
- Purged old unused code
- ComboBoxProperty updates selected item on the new model
- Fixed macOS shape rendering with multiply blend mode
- Fixed Linux shape rendering with multiply blend mode

### Other

- Set build destination to the root target directory
- Updated package version number
- Update to the improved zoom rectangle appearance
- Observe recorded changes on a more granular level
- Export resources to web-player directory
- Fixed rendering static annotations in slide previews
- Improved pointer appearance

<!-- generated by git-cliff -->
