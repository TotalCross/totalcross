// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.icon;

import totalcross.sys.Settings;

public enum MaterialIcons implements IconType {
  _3D_ROTATION('\ue84d'),
  _AC_UNIT('\ueb3b'),
  _ACCESS_ALARM('\ue190'),
  _ACCESS_ALARMS('\ue191'),
  _ACCESS_TIME('\ue192'),
  _ACCESSIBILITY('\ue84e'),
  _ACCESSIBLE('\ue914'),
  _ACCOUNT_BALANCE('\ue84f'),
  _ACCOUNT_BALANCE_WALLET('\ue850'),
  _ACCOUNT_BOX('\ue851'),
  _ACCOUNT_CIRCLE('\ue853'),
  _ADB('\ue60e'),
  _ADD('\ue145'),
  _ADD_A_PHOTO('\ue439'),
  _ADD_ALARM('\ue193'),
  _ADD_ALERT('\ue003'),
  _ADD_BOX('\ue146'),
  _ADD_CIRCLE('\ue147'),
  _ADD_CIRCLE_OUTLINE('\ue148'),
  _ADD_LOCATION('\ue567'),
  _ADD_SHOPPING_CART('\ue854'),
  _ADD_TO_PHOTOS('\ue39d'),
  _ADD_TO_QUEUE('\ue05c'),
  _ADJUST('\ue39e'),
  _AIRLINE_SEAT_FLAT('\ue630'),
  _AIRLINE_SEAT_FLAT_ANGLED('\ue631'),
  _AIRLINE_SEAT_INDIVIDUAL_SUITE('\ue632'),
  _AIRLINE_SEAT_LEGROOM_EXTRA('\ue633'),
  _AIRLINE_SEAT_LEGROOM_NORMAL('\ue634'),
  _AIRLINE_SEAT_LEGROOM_REDUCED('\ue635'),
  _AIRLINE_SEAT_RECLINE_EXTRA('\ue636'),
  _AIRLINE_SEAT_RECLINE_NORMAL('\ue637'),
  _AIRPLANEMODE_ACTIVE('\ue195'),
  _AIRPLANEMODE_INACTIVE('\ue194'),
  _AIRPLAY('\ue055'),
  _AIRPORT_SHUTTLE('\ueb3c'),
  _ALARM('\ue855'),
  _ALARM_ADD('\ue856'),
  _ALARM_OFF('\ue857'),
  _ALARM_ON('\ue858'),
  _ALBUM('\ue019'),
  _ALL_INCLUSIVE('\ueb3d'),
  _ALL_OUT('\ue90b'),
  _ANDROID('\ue859'),
  _ANNOUNCEMENT('\ue85a'),
  _APPS('\ue5c3'),
  _ARCHIVE('\ue149'),
  _ARROW_BACK('\ue5c4'),
  _ARROW_DOWNWARD('\ue5db'),
  _ARROW_DROP_DOWN('\ue5c5'),
  _ARROW_DROP_DOWN_CIRCLE('\ue5c6'),
  _ARROW_DROP_UP('\ue5c7'),
  _ARROW_FORWARD('\ue5c8'),
  _ARROW_UPWARD('\ue5d8'),
  _ART_TRACK('\ue060'),
  _ASPECT_RATIO('\ue85b'),
  _ASSESSMENT('\ue85c'),
  _ASSIGNMENT('\ue85d'),
  _ASSIGNMENT_IND('\ue85e'),
  _ASSIGNMENT_LATE('\ue85f'),
  _ASSIGNMENT_RETURN('\ue860'),
  _ASSIGNMENT_RETURNED('\ue861'),
  _ASSIGNMENT_TURNED_IN('\ue862'),
  _ASSISTANT('\ue39f'),
  _ASSISTANT_PHOTO('\ue3a0'),
  _ATTACH_FILE('\ue226'),
  _ATTACH_MONEY('\ue227'),
  _ATTACHMENT('\ue2bc'),
  _AUDIOTRACK('\ue3a1'),
  _AUTORENEW('\ue863'),
  _AV_TIMER('\ue01b'),
  _BACKSPACE('\ue14a'),
  _BACKUP('\ue864'),
  _BATTERY_ALERT('\ue19c'),
  _BATTERY_CHARGING_FULL('\ue1a3'),
  _BATTERY_FULL('\ue1a4'),
  _BATTERY_STD('\ue1a5'),
  _BATTERY_UNKNOWN('\ue1a6'),
  _BEACH_ACCESS('\ueb3e'),
  _BEENHERE('\ue52d'),
  _BLOCK('\ue14b'),
  _BLUETOOTH('\ue1a7'),
  _BLUETOOTH_AUDIO('\ue60f'),
  _BLUETOOTH_CONNECTED('\ue1a8'),
  _BLUETOOTH_DISABLED('\ue1a9'),
  _BLUETOOTH_SEARCHING('\ue1aa'),
  _BLUR_CIRCULAR('\ue3a2'),
  _BLUR_LINEAR('\ue3a3'),
  _BLUR_OFF('\ue3a4'),
  _BLUR_ON('\ue3a5'),
  _BOOK('\ue865'),
  _BOOKMARK('\ue866'),
  _BOOKMARK_BORDER('\ue867'),
  _BORDER_ALL('\ue228'),
  _BORDER_BOTTOM('\ue229'),
  _BORDER_CLEAR('\ue22a'),
  _BORDER_COLOR('\ue22b'),
  _BORDER_HORIZONTAL('\ue22c'),
  _BORDER_INNER('\ue22d'),
  _BORDER_LEFT('\ue22e'),
  _BORDER_OUTER('\ue22f'),
  _BORDER_RIGHT('\ue230'),
  _BORDER_STYLE('\ue231'),
  _BORDER_TOP('\ue232'),
  _BORDER_VERTICAL('\ue233'),
  _BRANDING_WATERMARK('\ue06b'),
  _BRIGHTNESS_1('\ue3a6'),
  _BRIGHTNESS_2('\ue3a7'),
  _BRIGHTNESS_3('\ue3a8'),
  _BRIGHTNESS_4('\ue3a9'),
  _BRIGHTNESS_5('\ue3aa'),
  _BRIGHTNESS_6('\ue3ab'),
  _BRIGHTNESS_7('\ue3ac'),
  _BRIGHTNESS_AUTO('\ue1ab'),
  _BRIGHTNESS_HIGH('\ue1ac'),
  _BRIGHTNESS_LOW('\ue1ad'),
  _BRIGHTNESS_MEDIUM('\ue1ae'),
  _BROKEN_IMAGE('\ue3ad'),
  _BRUSH('\ue3ae'),
  _BUBBLE_CHART('\ue6dd'),
  _BUG_REPORT('\ue868'),
  _BUILD('\ue869'),
  _BURST_MODE('\ue43c'),
  _BUSINESS('\ue0af'),
  _BUSINESS_CENTER('\ueb3f'),
  _CACHED('\ue86a'),
  _CAKE('\ue7e9'),
  _CALL('\ue0b0'),
  _CALL_END('\ue0b1'),
  _CALL_MADE('\ue0b2'),
  _CALL_MERGE('\ue0b3'),
  _CALL_MISSED('\ue0b4'),
  _CALL_MISSED_OUTGOING('\ue0e4'),
  _CALL_RECEIVED('\ue0b5'),
  _CALL_SPLIT('\ue0b6'),
  _CALL_TO_ACTION('\ue06c'),
  _CAMERA('\ue3af'),
  _CAMERA_ALT('\ue3b0'),
  _CAMERA_ENHANCE('\ue8fc'),
  _CAMERA_FRONT('\ue3b1'),
  _CAMERA_REAR('\ue3b2'),
  _CAMERA_ROLL('\ue3b3'),
  _CANCEL('\ue5c9'),
  _CARD_GIFTCARD('\ue8f6'),
  _CARD_MEMBERSHIP('\ue8f7'),
  _CARD_TRAVEL('\ue8f8'),
  _CASINO('\ueb40'),
  _CAST('\ue307'),
  _CAST_CONNECTED('\ue308'),
  _CENTER_FOCUS_STRONG('\ue3b4'),
  _CENTER_FOCUS_WEAK('\ue3b5'),
  _CHANGE_HISTORY('\ue86b'),
  _CHAT('\ue0b7'),
  _CHAT_BUBBLE('\ue0ca'),
  _CHAT_BUBBLE_OUTLINE('\ue0cb'),
  _CHECK('\ue5ca'),
  _CHECK_BOX('\ue834'),
  _CHECK_BOX_OUTLINE_BLANK('\ue835'),
  _CHECK_CIRCLE('\ue86c'),
  _CHEVRON_LEFT('\ue5cb'),
  _CHEVRON_RIGHT('\ue5cc'),
  _CHILD_CARE('\ueb41'),
  _CHILD_FRIENDLY('\ueb42'),
  _CHROME_READER_MODE('\ue86d'),
  _CLASS('\ue86e'),
  _CLEAR('\ue14c'),
  _CLEAR_ALL('\ue0b8'),
  _CLOSE('\ue5cd'),
  _CLOSED_CAPTION('\ue01c'),
  _CLOUD('\ue2bd'),
  _CLOUD_CIRCLE('\ue2be'),
  _CLOUD_DONE('\ue2bf'),
  _CLOUD_DOWNLOAD('\ue2c0'),
  _CLOUD_OFF('\ue2c1'),
  _CLOUD_QUEUE('\ue2c2'),
  _CLOUD_UPLOAD('\ue2c3'),
  _CODE('\ue86f'),
  _COLLECTIONS('\ue3b6'),
  _COLLECTIONS_BOOKMARK('\ue431'),
  _COLOR_LENS('\ue3b7'),
  _COLORIZE('\ue3b8'),
  _COMMENT('\ue0b9'),
  _COMPARE('\ue3b9'),
  _COMPARE_ARROWS('\ue915'),
  _COMPUTER('\ue30a'),
  _CONFIRMATION_NUMBER('\ue638'),
  _CONTACT_MAIL('\ue0d0'),
  _CONTACT_PHONE('\ue0cf'),
  _CONTACTS('\ue0ba'),
  _CONTENT_COPY('\ue14d'),
  _CONTENT_CUT('\ue14e'),
  _CONTENT_PASTE('\ue14f'),
  _CONTROL_POINT('\ue3ba'),
  _CONTROL_POINT_DUPLICATE('\ue3bb'),
  _COPYRIGHT('\ue90c'),
  _CREATE('\ue150'),
  _CREATE_NEW_FOLDER('\ue2cc'),
  _CREDIT_CARD('\ue870'),
  _CROP('\ue3be'),
  _CROP_16_9('\ue3bc'),
  _CROP_3_2('\ue3bd'),
  _CROP_5_4('\ue3bf'),
  _CROP_7_5('\ue3c0'),
  _CROP_DIN('\ue3c1'),
  _CROP_FREE('\ue3c2'),
  _CROP_LANDSCAPE('\ue3c3'),
  _CROP_ORIGINAL('\ue3c4'),
  _CROP_PORTRAIT('\ue3c5'),
  _CROP_ROTATE('\ue437'),
  _CROP_SQUARE('\ue3c6'),
  _DASHBOARD('\ue871'),
  _DATA_USAGE('\ue1af'),
  _DATE_RANGE('\ue916'),
  _DEHAZE('\ue3c7'),
  _DELETE('\ue872'),
  _DELETE_FOREVER('\ue92b'),
  _DELETE_SWEEP('\ue16c'),
  _DESCRIPTION('\ue873'),
  _DESKTOP_MAC('\ue30b'),
  _DESKTOP_WINDOWS('\ue30c'),
  _DETAILS('\ue3c8'),
  _DEVELOPER_BOARD('\ue30d'),
  _DEVELOPER_MODE('\ue1b0'),
  _DEVICE_HUB('\ue335'),
  _DEVICES('\ue1b1'),
  _DEVICES_OTHER('\ue337'),
  _DIALER_SIP('\ue0bb'),
  _DIALPAD('\ue0bc'),
  _DIRECTIONS('\ue52e'),
  _DIRECTIONS_BIKE('\ue52f'),
  _DIRECTIONS_BOAT('\ue532'),
  _DIRECTIONS_BUS('\ue530'),
  _DIRECTIONS_CAR('\ue531'),
  _DIRECTIONS_RAILWAY('\ue534'),
  _DIRECTIONS_RUN('\ue566'),
  _DIRECTIONS_SUBWAY('\ue533'),
  _DIRECTIONS_TRANSIT('\ue535'),
  _DIRECTIONS_WALK('\ue536'),
  _DISC_FULL('\ue610'),
  _DNS('\ue875'),
  _DO_NOT_DISTURB('\ue612'),
  _DO_NOT_DISTURB_ALT('\ue611'),
  _DO_NOT_DISTURB_OFF('\ue643'),
  _DO_NOT_DISTURB_ON('\ue644'),
  _DOCK('\ue30e'),
  _DOMAIN('\ue7ee'),
  _DONE('\ue876'),
  _DONE_ALL('\ue877'),
  _DONUT_LARGE('\ue917'),
  _DONUT_SMALL('\ue918'),
  _DRAFTS('\ue151'),
  _DRAG_HANDLE('\ue25d'),
  _DRIVE_ETA('\ue613'),
  _DVR('\ue1b2'),
  _EDIT('\ue3c9'),
  _EDIT_LOCATION('\ue568'),
  _EJECT('\ue8fb'),
  _EMAIL('\ue0be'),
  _ENHANCED_ENCRYPTION('\ue63f'),
  _EQUALIZER('\ue01d'),
  _ERROR('\ue000'),
  _ERROR_OUTLINE('\ue001'),
  _EURO_SYMBOL('\ue926'),
  _EV_STATION('\ue56d'),
  _EVENT('\ue878'),
  _EVENT_AVAILABLE('\ue614'),
  _EVENT_BUSY('\ue615'),
  _EVENT_NOTE('\ue616'),
  _EVENT_SEAT('\ue903'),
  _EXIT_TO_APP('\ue879'),
  _EXPAND_LESS('\ue5ce'),
  _EXPAND_MORE('\ue5cf'),
  _EXPLICIT('\ue01e'),
  _EXPLORE('\ue87a'),
  _EXPOSURE('\ue3ca'),
  _EXPOSURE_NEG_1('\ue3cb'),
  _EXPOSURE_NEG_2('\ue3cc'),
  _EXPOSURE_PLUS_1('\ue3cd'),
  _EXPOSURE_PLUS_2('\ue3ce'),
  _EXPOSURE_ZERO('\ue3cf'),
  _EXTENSION('\ue87b'),
  _FACE('\ue87c'),
  _FAST_FORWARD('\ue01f'),
  _FAST_REWIND('\ue020'),
  _FAVORITE('\ue87d'),
  _FAVORITE_BORDER('\ue87e'),
  _FEATURED_PLAY_LIST('\ue06d'),
  _FEATURED_VIDEO('\ue06e'),
  _FEEDBACK('\ue87f'),
  _FIBER_DVR('\ue05d'),
  _FIBER_MANUAL_RECORD('\ue061'),
  _FIBER_NEW('\ue05e'),
  _FIBER_PIN('\ue06a'),
  _FIBER_SMART_RECORD('\ue062'),
  _FILE_DOWNLOAD('\ue2c4'),
  _FILE_UPLOAD('\ue2c6'),
  _FILTER('\ue3d3'),
  _FILTER_1('\ue3d0'),
  _FILTER_2('\ue3d1'),
  _FILTER_3('\ue3d2'),
  _FILTER_4('\ue3d4'),
  _FILTER_5('\ue3d5'),
  _FILTER_6('\ue3d6'),
  _FILTER_7('\ue3d7'),
  _FILTER_8('\ue3d8'),
  _FILTER_9('\ue3d9'),
  _FILTER_9_PLUS('\ue3da'),
  _FILTER_B_AND_W('\ue3db'),
  _FILTER_CENTER_FOCUS('\ue3dc'),
  _FILTER_DRAMA('\ue3dd'),
  _FILTER_FRAMES('\ue3de'),
  _FILTER_HDR('\ue3df'),
  _FILTER_LIST('\ue152'),
  _FILTER_NONE('\ue3e0'),
  _FILTER_TILT_SHIFT('\ue3e2'),
  _FILTER_VINTAGE('\ue3e3'),
  _FIND_IN_PAGE('\ue880'),
  _FIND_REPLACE('\ue881'),
  _FINGERPRINT('\ue90d'),
  _FIRST_PAGE('\ue5dc'),
  _FITNESS_CENTER('\ueb43'),
  _FLAG('\ue153'),
  _FLARE('\ue3e4'),
  _FLASH_AUTO('\ue3e5'),
  _FLASH_OFF('\ue3e6'),
  _FLASH_ON('\ue3e7'),
  _FLIGHT('\ue539'),
  _FLIGHT_LAND('\ue904'),
  _FLIGHT_TAKEOFF('\ue905'),
  _FLIP('\ue3e8'),
  _FLIP_TO_BACK('\ue882'),
  _FLIP_TO_FRONT('\ue883'),
  _FOLDER('\ue2c7'),
  _FOLDER_OPEN('\ue2c8'),
  _FOLDER_SHARED('\ue2c9'),
  _FOLDER_SPECIAL('\ue617'),
  _FONT_DOWNLOAD('\ue167'),
  _FORMAT_ALIGN_CENTER('\ue234'),
  _FORMAT_ALIGN_JUSTIFY('\ue235'),
  _FORMAT_ALIGN_LEFT('\ue236'),
  _FORMAT_ALIGN_RIGHT('\ue237'),
  _FORMAT_BOLD('\ue238'),
  _FORMAT_CLEAR('\ue239'),
  _FORMAT_COLOR_FILL('\ue23a'),
  _FORMAT_COLOR_RESET('\ue23b'),
  _FORMAT_COLOR_TEXT('\ue23c'),
  _FORMAT_INDENT_DECREASE('\ue23d'),
  _FORMAT_INDENT_INCREASE('\ue23e'),
  _FORMAT_ITALIC('\ue23f'),
  _FORMAT_LINE_SPACING('\ue240'),
  _FORMAT_LIST_BULLETED('\ue241'),
  _FORMAT_LIST_NUMBERED('\ue242'),
  _FORMAT_PAINT('\ue243'),
  _FORMAT_QUOTE('\ue244'),
  _FORMAT_SHAPES('\ue25e'),
  _FORMAT_SIZE('\ue245'),
  _FORMAT_STRIKETHROUGH('\ue246'),
  _FORMAT_TEXTDIRECTION_L_TO_R('\ue247'),
  _FORMAT_TEXTDIRECTION_R_TO_L('\ue248'),
  _FORMAT_UNDERLINED('\ue249'),
  _FORUM('\ue0bf'),
  _FORWARD('\ue154'),
  _FORWARD_10('\ue056'),
  _FORWARD_30('\ue057'),
  _FORWARD_5('\ue058'),
  _FREE_BREAKFAST('\ueb44'),
  _FULLSCREEN('\ue5d0'),
  _FULLSCREEN_EXIT('\ue5d1'),
  _FUNCTIONS('\ue24a'),
  _G_TRANSLATE('\ue927'),
  _GAMEPAD('\ue30f'),
  _GAMES('\ue021'),
  _GAVEL('\ue90e'),
  _GESTURE('\ue155'),
  _GET_APP('\ue884'),
  _GIF('\ue908'),
  _GOLF_COURSE('\ueb45'),
  _GPS_FIXED('\ue1b3'),
  _GPS_NOT_FIXED('\ue1b4'),
  _GPS_OFF('\ue1b5'),
  _GRADE('\ue885'),
  _GRADIENT('\ue3e9'),
  _GRAIN('\ue3ea'),
  _GRAPHIC_EQ('\ue1b8'),
  _GRID_OFF('\ue3eb'),
  _GRID_ON('\ue3ec'),
  _GROUP('\ue7ef'),
  _GROUP_ADD('\ue7f0'),
  _GROUP_WORK('\ue886'),
  _HD('\ue052'),
  _HDR_OFF('\ue3ed'),
  _HDR_ON('\ue3ee'),
  _HDR_STRONG('\ue3f1'),
  _HDR_WEAK('\ue3f2'),
  _HEADSET('\ue310'),
  _HEADSET_MIC('\ue311'),
  _HEALING('\ue3f3'),
  _HEARING('\ue023'),
  _HELP('\ue887'),
  _HELP_OUTLINE('\ue8fd'),
  _HIGH_QUALITY('\ue024'),
  _HIGHLIGHT('\ue25f'),
  _HIGHLIGHT_OFF('\ue888'),
  _HISTORY('\ue889'),
  _HOME('\ue88a'),
  _HOT_TUB('\ueb46'),
  _HOTEL('\ue53a'),
  _HOURGLASS_EMPTY('\ue88b'),
  _HOURGLASS_FULL('\ue88c'),
  _HTTP('\ue902'),
  _HTTPS('\ue88d'),
  _IMAGE('\ue3f4'),
  _IMAGE_ASPECT_RATIO('\ue3f5'),
  _IMPORT_CONTACTS('\ue0e0'),
  _IMPORT_EXPORT('\ue0c3'),
  _IMPORTANT_DEVICES('\ue912'),
  _INBOX('\ue156'),
  _INDETERMINATE_CHECK_BOX('\ue909'),
  _INFO('\ue88e'),
  _INFO_OUTLINE('\ue88f'),
  _INPUT('\ue890'),
  _INSERT_CHART('\ue24b'),
  _INSERT_COMMENT('\ue24c'),
  _INSERT_DRIVE_FILE('\ue24d'),
  _INSERT_EMOTICON('\ue24e'),
  _INSERT_INVITATION('\ue24f'),
  _INSERT_LINK('\ue250'),
  _INSERT_PHOTO('\ue251'),
  _INVERT_COLORS('\ue891'),
  _INVERT_COLORS_OFF('\ue0c4'),
  _ISO('\ue3f6'),
  _KEYBOARD('\ue312'),
  _KEYBOARD_ARROW_DOWN('\ue313'),
  _KEYBOARD_ARROW_LEFT('\ue314'),
  _KEYBOARD_ARROW_RIGHT('\ue315'),
  _KEYBOARD_ARROW_UP('\ue316'),
  _KEYBOARD_BACKSPACE('\ue317'),
  _KEYBOARD_CAPSLOCK('\ue318'),
  _KEYBOARD_HIDE('\ue31a'),
  _KEYBOARD_RETURN('\ue31b'),
  _KEYBOARD_TAB('\ue31c'),
  _KEYBOARD_VOICE('\ue31d'),
  _KITCHEN('\ueb47'),
  _LABEL('\ue892'),
  _LABEL_OUTLINE('\ue893'),
  _LANDSCAPE('\ue3f7'),
  _LANGUAGE('\ue894'),
  _LAPTOP('\ue31e'),
  _LAPTOP_CHROMEBOOK('\ue31f'),
  _LAPTOP_MAC('\ue320'),
  _LAPTOP_WINDOWS('\ue321'),
  _LAST_PAGE('\ue5dd'),
  _LAUNCH('\ue895'),
  _LAYERS('\ue53b'),
  _LAYERS_CLEAR('\ue53c'),
  _LEAK_ADD('\ue3f8'),
  _LEAK_REMOVE('\ue3f9'),
  _LENS('\ue3fa'),
  _LIBRARY_ADD('\ue02e'),
  _LIBRARY_BOOKS('\ue02f'),
  _LIBRARY_MUSIC('\ue030'),
  _LIGHTBULB_OUTLINE('\ue90f'),
  _LINE_STYLE('\ue919'),
  _LINE_WEIGHT('\ue91a'),
  _LINEAR_SCALE('\ue260'),
  _LINK('\ue157'),
  _LINKED_CAMERA('\ue438'),
  _LIST('\ue896'),
  _LIVE_HELP('\ue0c6'),
  _LIVE_TV('\ue639'),
  _LOCAL_ACTIVITY('\ue53f'),
  _LOCAL_AIRPORT('\ue53d'),
  _LOCAL_ATM('\ue53e'),
  _LOCAL_BAR('\ue540'),
  _LOCAL_CAFE('\ue541'),
  _LOCAL_CAR_WASH('\ue542'),
  _LOCAL_CONVENIENCE_STORE('\ue543'),
  _LOCAL_DINING('\ue556'),
  _LOCAL_DRINK('\ue544'),
  _LOCAL_FLORIST('\ue545'),
  _LOCAL_GAS_STATION('\ue546'),
  _LOCAL_GROCERY_STORE('\ue547'),
  _LOCAL_HOSPITAL('\ue548'),
  _LOCAL_HOTEL('\ue549'),
  _LOCAL_LAUNDRY_SERVICE('\ue54a'),
  _LOCAL_LIBRARY('\ue54b'),
  _LOCAL_MALL('\ue54c'),
  _LOCAL_MOVIES('\ue54d'),
  _LOCAL_OFFER('\ue54e'),
  _LOCAL_PARKING('\ue54f'),
  _LOCAL_PHARMACY('\ue550'),
  _LOCAL_PHONE('\ue551'),
  _LOCAL_PIZZA('\ue552'),
  _LOCAL_PLAY('\ue553'),
  _LOCAL_POST_OFFICE('\ue554'),
  _LOCAL_PRINTSHOP('\ue555'),
  _LOCAL_SEE('\ue557'),
  _LOCAL_SHIPPING('\ue558'),
  _LOCAL_TAXI('\ue559'),
  _LOCATION_CITY('\ue7f1'),
  _LOCATION_DISABLED('\ue1b6'),
  _LOCATION_OFF('\ue0c7'),
  _LOCATION_ON('\ue0c8'),
  _LOCATION_SEARCHING('\ue1b7'),
  _LOCK('\ue897'),
  _LOCK_OPEN('\ue898'),
  _LOCK_OUTLINE('\ue899'),
  _LOOKS('\ue3fc'),
  _LOOKS_3('\ue3fb'),
  _LOOKS_4('\ue3fd'),
  _LOOKS_5('\ue3fe'),
  _LOOKS_6('\ue3ff'),
  _LOOKS_ONE('\ue400'),
  _LOOKS_TWO('\ue401'),
  _LOOP('\ue028'),
  _LOUPE('\ue402'),
  _LOW_PRIORITY('\ue16d'),
  _LOYALTY('\ue89a'),
  _MAIL('\ue158'),
  _MAIL_OUTLINE('\ue0e1'),
  _MAP('\ue55b'),
  _MARKUNREAD('\ue159'),
  _MARKUNREAD_MAILBOX('\ue89b'),
  _MEMORY('\ue322'),
  _MENU('\ue5d2'),
  _MERGE_TYPE('\ue252'),
  _MESSAGE('\ue0c9'),
  _MIC('\ue029'),
  _MIC_NONE('\ue02a'),
  _MIC_OFF('\ue02b'),
  _MMS('\ue618'),
  _MODE_COMMENT('\ue253'),
  _MODE_EDIT('\ue254'),
  _MONETIZATION_ON('\ue263'),
  _MONEY_OFF('\ue25c'),
  _MONOCHROME_PHOTOS('\ue403'),
  _MOOD('\ue7f2'),
  _MOOD_BAD('\ue7f3'),
  _MORE('\ue619'),
  _MORE_HORIZ('\ue5d3'),
  _MORE_VERT('\ue5d4'),
  _MOTORCYCLE('\ue91b'),
  _MOUSE('\ue323'),
  _MOVE_TO_INBOX('\ue168'),
  _MOVIE('\ue02c'),
  _MOVIE_CREATION('\ue404'),
  _MOVIE_FILTER('\ue43a'),
  _MULTILINE_CHART('\ue6df'),
  _MUSIC_NOTE('\ue405'),
  _MUSIC_VIDEO('\ue063'),
  _MY_LOCATION('\ue55c'),
  _NATURE('\ue406'),
  _NATURE_PEOPLE('\ue407'),
  _NAVIGATE_BEFORE('\ue408'),
  _NAVIGATE_NEXT('\ue409'),
  _NAVIGATION('\ue55d'),
  _NEAR_ME('\ue569'),
  _NETWORK_CELL('\ue1b9'),
  _NETWORK_CHECK('\ue640'),
  _NETWORK_LOCKED('\ue61a'),
  _NETWORK_WIFI('\ue1ba'),
  _NEW_RELEASES('\ue031'),
  _NEXT_WEEK('\ue16a'),
  _NFC('\ue1bb'),
  _NO_ENCRYPTION('\ue641'),
  _NO_SIM('\ue0cc'),
  _NOT_INTERESTED('\ue033'),
  _NOTE('\ue06f'),
  _NOTE_ADD('\ue89c'),
  _NOTIFICATIONS('\ue7f4'),
  _NOTIFICATIONS_ACTIVE('\ue7f7'),
  _NOTIFICATIONS_NONE('\ue7f5'),
  _NOTIFICATIONS_OFF('\ue7f6'),
  _NOTIFICATIONS_PAUSED('\ue7f8'),
  _OFFLINE_PIN('\ue90a'),
  _ONDEMAND_VIDEO('\ue63a'),
  _OPACITY('\ue91c'),
  _OPEN_IN_BROWSER('\ue89d'),
  _OPEN_IN_NEW('\ue89e'),
  _OPEN_WITH('\ue89f'),
  _PAGES('\ue7f9'),
  _PAGEVIEW('\ue8a0'),
  _PALETTE('\ue40a'),
  _PAN_TOOL('\ue925'),
  _PANORAMA('\ue40b'),
  _PANORAMA_FISH_EYE('\ue40c'),
  _PANORAMA_HORIZONTAL('\ue40d'),
  _PANORAMA_VERTICAL('\ue40e'),
  _PANORAMA_WIDE_ANGLE('\ue40f'),
  _PARTY_MODE('\ue7fa'),
  _PAUSE('\ue034'),
  _PAUSE_CIRCLE_FILLED('\ue035'),
  _PAUSE_CIRCLE_OUTLINE('\ue036'),
  _PAYMENT('\ue8a1'),
  _PEOPLE('\ue7fb'),
  _PEOPLE_OUTLINE('\ue7fc'),
  _PERM_CAMERA_MIC('\ue8a2'),
  _PERM_CONTACT_CALENDAR('\ue8a3'),
  _PERM_DATA_SETTING('\ue8a4'),
  _PERM_DEVICE_INFORMATION('\ue8a5'),
  _PERM_IDENTITY('\ue8a6'),
  _PERM_MEDIA('\ue8a7'),
  _PERM_PHONE_MSG('\ue8a8'),
  _PERM_SCAN_WIFI('\ue8a9'),
  _PERSON('\ue7fd'),
  _PERSON_ADD('\ue7fe'),
  _PERSON_OUTLINE('\ue7ff'),
  _PERSON_PIN('\ue55a'),
  _PERSON_PIN_CIRCLE('\ue56a'),
  _PERSONAL_VIDEO('\ue63b'),
  _PETS('\ue91d'),
  _PHONE('\ue0cd'),
  _PHONE_ANDROID('\ue324'),
  _PHONE_BLUETOOTH_SPEAKER('\ue61b'),
  _PHONE_FORWARDED('\ue61c'),
  _PHONE_IN_TALK('\ue61d'),
  _PHONE_IPHONE('\ue325'),
  _PHONE_LOCKED('\ue61e'),
  _PHONE_MISSED('\ue61f'),
  _PHONE_PAUSED('\ue620'),
  _PHONELINK('\ue326'),
  _PHONELINK_ERASE('\ue0db'),
  _PHONELINK_LOCK('\ue0dc'),
  _PHONELINK_OFF('\ue327'),
  _PHONELINK_RING('\ue0dd'),
  _PHONELINK_SETUP('\ue0de'),
  _PHOTO('\ue410'),
  _PHOTO_ALBUM('\ue411'),
  _PHOTO_CAMERA('\ue412'),
  _PHOTO_FILTER('\ue43b'),
  _PHOTO_LIBRARY('\ue413'),
  _PHOTO_SIZE_SELECT_ACTUAL('\ue432'),
  _PHOTO_SIZE_SELECT_LARGE('\ue433'),
  _PHOTO_SIZE_SELECT_SMALL('\ue434'),
  _PICTURE_AS_PDF('\ue415'),
  _PICTURE_IN_PICTURE('\ue8aa'),
  _PICTURE_IN_PICTURE_ALT('\ue911'),
  _PIE_CHART('\ue6c4'),
  _PIE_CHART_OUTLINED('\ue6c5'),
  _PIN_DROP('\ue55e'),
  _PLACE('\ue55f'),
  _PLAY_ARROW('\ue037'),
  _PLAY_CIRCLE_FILLED('\ue038'),
  _PLAY_CIRCLE_OUTLINE('\ue039'),
  _PLAY_FOR_WORK('\ue906'),
  _PLAYLIST_ADD('\ue03b'),
  _PLAYLIST_ADD_CHECK('\ue065'),
  _PLAYLIST_PLAY('\ue05f'),
  _PLUS_ONE('\ue800'),
  _POLL('\ue801'),
  _POLYMER('\ue8ab'),
  _POOL('\ueb48'),
  _PORTABLE_WIFI_OFF('\ue0ce'),
  _PORTRAIT('\ue416'),
  _POWER('\ue63c'),
  _POWER_INPUT('\ue336'),
  _POWER_SETTINGS_NEW('\ue8ac'),
  _PREGNANT_WOMAN('\ue91e'),
  _PRESENT_TO_ALL('\ue0df'),
  _PRINT('\ue8ad'),
  _PRIORITY_HIGH('\ue645'),
  _PUBLIC('\ue80b'),
  _PUBLISH('\ue255'),
  _QUERY_BUILDER('\ue8ae'),
  _QUESTION_ANSWER('\ue8af'),
  _QUEUE('\ue03c'),
  _QUEUE_MUSIC('\ue03d'),
  _QUEUE_PLAY_NEXT('\ue066'),
  _RADIO('\ue03e'),
  _RADIO_BUTTON_CHECKED('\ue837'),
  _RADIO_BUTTON_UNCHECKED('\ue836'),
  _RATE_REVIEW('\ue560'),
  _RECEIPT('\ue8b0'),
  _RECENT_ACTORS('\ue03f'),
  _RECORD_VOICE_OVER('\ue91f'),
  _REDEEM('\ue8b1'),
  _REDO('\ue15a'),
  _REFRESH('\ue5d5'),
  _REMOVE('\ue15b'),
  _REMOVE_CIRCLE('\ue15c'),
  _REMOVE_CIRCLE_OUTLINE('\ue15d'),
  _REMOVE_FROM_QUEUE('\ue067'),
  _REMOVE_RED_EYE('\ue417'),
  _REMOVE_SHOPPING_CART('\ue928'),
  _REORDER('\ue8fe'),
  _REPEAT('\ue040'),
  _REPEAT_ONE('\ue041'),
  _REPLAY('\ue042'),
  _REPLAY_10('\ue059'),
  _REPLAY_30('\ue05a'),
  _REPLAY_5('\ue05b'),
  _REPLY('\ue15e'),
  _REPLY_ALL('\ue15f'),
  _REPORT('\ue160'),
  _REPORT_PROBLEM('\ue8b2'),
  _RESTAURANT('\ue56c'),
  _RESTAURANT_MENU('\ue561'),
  _RESTORE('\ue8b3'),
  _RESTORE_PAGE('\ue929'),
  _RING_VOLUME('\ue0d1'),
  _ROOM('\ue8b4'),
  _ROOM_SERVICE('\ueb49'),
  _ROTATE_90_DEGREES_CCW('\ue418'),
  _ROTATE_LEFT('\ue419'),
  _ROTATE_RIGHT('\ue41a'),
  _ROUNDED_CORNER('\ue920'),
  _ROUTER('\ue328'),
  _ROWING('\ue921'),
  _RSS_FEED('\ue0e5'),
  _RV_HOOKUP('\ue642'),
  _SATELLITE('\ue562'),
  _SAVE('\ue161'),
  _SCANNER('\ue329'),
  _SCHEDULE('\ue8b5'),
  _SCHOOL('\ue80c'),
  _SCREEN_LOCK_LANDSCAPE('\ue1be'),
  _SCREEN_LOCK_PORTRAIT('\ue1bf'),
  _SCREEN_LOCK_ROTATION('\ue1c0'),
  _SCREEN_ROTATION('\ue1c1'),
  _SCREEN_SHARE('\ue0e2'),
  _SD_CARD('\ue623'),
  _SD_STORAGE('\ue1c2'),
  _SEARCH('\ue8b6'),
  _SECURITY('\ue32a'),
  _SELECT_ALL('\ue162'),
  _SEND('\ue163'),
  _SENTIMENT_DISSATISFIED('\ue811'),
  _SENTIMENT_NEUTRAL('\ue812'),
  _SENTIMENT_SATISFIED('\ue813'),
  _SENTIMENT_VERY_DISSATISFIED('\ue814'),
  _SENTIMENT_VERY_SATISFIED('\ue815'),
  _SETTINGS('\ue8b8'),
  _SETTINGS_APPLICATIONS('\ue8b9'),
  _SETTINGS_BACKUP_RESTORE('\ue8ba'),
  _SETTINGS_BLUETOOTH('\ue8bb'),
  _SETTINGS_BRIGHTNESS('\ue8bd'),
  _SETTINGS_CELL('\ue8bc'),
  _SETTINGS_ETHERNET('\ue8be'),
  _SETTINGS_INPUT_ANTENNA('\ue8bf'),
  _SETTINGS_INPUT_COMPONENT('\ue8c0'),
  _SETTINGS_INPUT_COMPOSITE('\ue8c1'),
  _SETTINGS_INPUT_HDMI('\ue8c2'),
  _SETTINGS_INPUT_SVIDEO('\ue8c3'),
  _SETTINGS_OVERSCAN('\ue8c4'),
  _SETTINGS_PHONE('\ue8c5'),
  _SETTINGS_POWER('\ue8c6'),
  _SETTINGS_REMOTE('\ue8c7'),
  _SETTINGS_SYSTEM_DAYDREAM('\ue1c3'),
  _SETTINGS_VOICE('\ue8c8'),
  _SHARE('\ue80d'),
  _SHOP('\ue8c9'),
  _SHOP_TWO('\ue8ca'),
  _SHOPPING_BASKET('\ue8cb'),
  _SHOPPING_CART('\ue8cc'),
  _SHORT_TEXT('\ue261'),
  _SHOW_CHART('\ue6e1'),
  _SHUFFLE('\ue043'),
  _SIGNAL_CELLULAR_4_BAR('\ue1c8'),
  _SIGNAL_CELLULAR_CONNECTED_NO_INTERNET_4_BAR('\ue1cd'),
  _SIGNAL_CELLULAR_NO_SIM('\ue1ce'),
  _SIGNAL_CELLULAR_NULL('\ue1cf'),
  _SIGNAL_CELLULAR_OFF('\ue1d0'),
  _SIGNAL_WIFI_4_BAR('\ue1d8'),
  _SIGNAL_WIFI_4_BAR_LOCK('\ue1d9'),
  _SIGNAL_WIFI_OFF('\ue1da'),
  _SIM_CARD('\ue32b'),
  _SIM_CARD_ALERT('\ue624'),
  _SKIP_NEXT('\ue044'),
  _SKIP_PREVIOUS('\ue045'),
  _SLIDESHOW('\ue41b'),
  _SLOW_MOTION_VIDEO('\ue068'),
  _SMARTPHONE('\ue32c'),
  _SMOKE_FREE('\ueb4a'),
  _SMOKING_ROOMS('\ueb4b'),
  _SMS('\ue625'),
  _SMS_FAILED('\ue626'),
  _SNOOZE('\ue046'),
  _SORT('\ue164'),
  _SORT_BY_ALPHA('\ue053'),
  _SPA('\ueb4c'),
  _SPACE_BAR('\ue256'),
  _SPEAKER('\ue32d'),
  _SPEAKER_GROUP('\ue32e'),
  _SPEAKER_NOTES('\ue8cd'),
  _SPEAKER_NOTES_OFF('\ue92a'),
  _SPEAKER_PHONE('\ue0d2'),
  _SPELLCHECK('\ue8ce'),
  _STAR('\ue838'),
  _STAR_BORDER('\ue83a'),
  _STAR_HALF('\ue839'),
  _STARS('\ue8d0'),
  _STAY_CURRENT_LANDSCAPE('\ue0d3'),
  _STAY_CURRENT_PORTRAIT('\ue0d4'),
  _STAY_PRIMARY_LANDSCAPE('\ue0d5'),
  _STAY_PRIMARY_PORTRAIT('\ue0d6'),
  _STOP('\ue047'),
  _STOP_SCREEN_SHARE('\ue0e3'),
  _STORAGE('\ue1db'),
  _STORE('\ue8d1'),
  _STORE_MALL_DIRECTORY('\ue563'),
  _STRAIGHTEN('\ue41c'),
  _STREETVIEW('\ue56e'),
  _STRIKETHROUGH_S('\ue257'),
  _STYLE('\ue41d'),
  _SUBDIRECTORY_ARROW_LEFT('\ue5d9'),
  _SUBDIRECTORY_ARROW_RIGHT('\ue5da'),
  _SUBJECT('\ue8d2'),
  _SUBSCRIPTIONS('\ue064'),
  _SUBTITLES('\ue048'),
  _SUBWAY('\ue56f'),
  _SUPERVISOR_ACCOUNT('\ue8d3'),
  _SURROUND_SOUND('\ue049'),
  _SWAP_CALLS('\ue0d7'),
  _SWAP_HORIZ('\ue8d4'),
  _SWAP_VERT('\ue8d5'),
  _SWAP_VERTICAL_CIRCLE('\ue8d6'),
  _SWITCH_CAMERA('\ue41e'),
  _SWITCH_VIDEO('\ue41f'),
  _SYNC('\ue627'),
  _SYNC_DISABLED('\ue628'),
  _SYNC_PROBLEM('\ue629'),
  _SYSTEM_UPDATE('\ue62a'),
  _SYSTEM_UPDATE_ALT('\ue8d7'),
  _TAB('\ue8d8'),
  _TAB_UNSELECTED('\ue8d9'),
  _TABLET('\ue32f'),
  _TABLET_ANDROID('\ue330'),
  _TABLET_MAC('\ue331'),
  _TAG_FACES('\ue420'),
  _TAP_AND_PLAY('\ue62b'),
  _TERRAIN('\ue564'),
  _TEXT_FIELDS('\ue262'),
  _TEXT_FORMAT('\ue165'),
  _TEXTSMS('\ue0d8'),
  _TEXTURE('\ue421'),
  _THEATERS('\ue8da'),
  _THUMB_DOWN('\ue8db'),
  _THUMB_UP('\ue8dc'),
  _THUMBS_UP_DOWN('\ue8dd'),
  _TIME_TO_LEAVE('\ue62c'),
  _TIMELAPSE('\ue422'),
  _TIMELINE('\ue922'),
  _TIMER('\ue425'),
  _TIMER_10('\ue423'),
  _TIMER_3('\ue424'),
  _TIMER_OFF('\ue426'),
  _TITLE('\ue264'),
  _TOC('\ue8de'),
  _TODAY('\ue8df'),
  _TOLL('\ue8e0'),
  _TONALITY('\ue427'),
  _TOUCH_APP('\ue913'),
  _TOYS('\ue332'),
  _TRACK_CHANGES('\ue8e1'),
  _TRAFFIC('\ue565'),
  _TRAIN('\ue570'),
  _TRAM('\ue571'),
  _TRANSFER_WITHIN_A_STATION('\ue572'),
  _TRANSFORM('\ue428'),
  _TRANSLATE('\ue8e2'),
  _TRENDING_DOWN('\ue8e3'),
  _TRENDING_FLAT('\ue8e4'),
  _TRENDING_UP('\ue8e5'),
  _TUNE('\ue429'),
  _TURNED_IN('\ue8e6'),
  _TURNED_IN_NOT('\ue8e7'),
  _TV('\ue333'),
  _UNARCHIVE('\ue169'),
  _UNDO('\ue166'),
  _UNFOLD_LESS('\ue5d6'),
  _UNFOLD_MORE('\ue5d7'),
  _UPDATE('\ue923'),
  _USB('\ue1e0'),
  _VERIFIED_USER('\ue8e8'),
  _VERTICAL_ALIGN_BOTTOM('\ue258'),
  _VERTICAL_ALIGN_CENTER('\ue259'),
  _VERTICAL_ALIGN_TOP('\ue25a'),
  _VIBRATION('\ue62d'),
  _VIDEO_CALL('\ue070'),
  _VIDEO_LABEL('\ue071'),
  _VIDEO_LIBRARY('\ue04a'),
  _VIDEOCAM('\ue04b'),
  _VIDEOCAM_OFF('\ue04c'),
  _VIDEOGAME_ASSET('\ue338'),
  _VIEW_AGENDA('\ue8e9'),
  _VIEW_ARRAY('\ue8ea'),
  _VIEW_CAROUSEL('\ue8eb'),
  _VIEW_COLUMN('\ue8ec'),
  _VIEW_COMFY('\ue42a'),
  _VIEW_COMPACT('\ue42b'),
  _VIEW_DAY('\ue8ed'),
  _VIEW_HEADLINE('\ue8ee'),
  _VIEW_LIST('\ue8ef'),
  _VIEW_MODULE('\ue8f0'),
  _VIEW_QUILT('\ue8f1'),
  _VIEW_STREAM('\ue8f2'),
  _VIEW_WEEK('\ue8f3'),
  _VIGNETTE('\ue435'),
  _VISIBILITY('\ue8f4'),
  _VISIBILITY_OFF('\ue8f5'),
  _VOICE_CHAT('\ue62e'),
  _VOICEMAIL('\ue0d9'),
  _VOLUME_DOWN('\ue04d'),
  _VOLUME_MUTE('\ue04e'),
  _VOLUME_OFF('\ue04f'),
  _VOLUME_UP('\ue050'),
  _VPN_KEY('\ue0da'),
  _VPN_LOCK('\ue62f'),
  _WALLPAPER('\ue1bc'),
  _WARNING('\ue002'),
  _WATCH('\ue334'),
  _WATCH_LATER('\ue924'),
  _WB_AUTO('\ue42c'),
  _WB_CLOUDY('\ue42d'),
  _WB_INCANDESCENT('\ue42e'),
  _WB_IRIDESCENT('\ue436'),
  _WB_SUNNY('\ue430'),
  _WC('\ue63d'),
  _WEB('\ue051'),
  _WEB_ASSET('\ue069'),
  _WEEKEND('\ue16b'),
  _WHATSHOT('\ue80e'),
  _WIDGETS('\ue1bd'),
  _WIFI('\ue63e'),
  _WIFI_LOCK('\ue1e1'),
  _WIFI_TETHERING('\ue1e2'),
  _WORK('\ue8f9'),
  _WRAP_TEXT('\ue25b'),
  _YOUTUBE_SEARCHED_FOR('\ue8fa'),
  _ZOOM_IN('\ue8ff'),
  _ZOOM_OUT('\ue900'),
  _ZOOM_OUT_MAP('\ue56b'),
  ;
    
  private int codepoint;

  private MaterialIcons(int codepoint) {
    this.codepoint = codepoint;
  }

  @Override
  public String toString() {
    return String.valueOf((char) codepoint);
  }

  @Override
  public String fontName() {
	  if (Settings.isOpenGL) {
		  return "MaterialIcons-Regular";
	  } else {
		  return "Material Icons";
	  }
  }

  @Override
  public int codepoint() {
    return codepoint;
  }
}
