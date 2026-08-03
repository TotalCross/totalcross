# Provenance report: `totalcross.Launcher`

- Initial revision: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Final revision: `07b9cd3b3bf154ab7ed7498b7a51a5b9e75c3cda`
- Historical source: `TotalCrossSDK/src/main/java/totalcross/Launcher.java`
- Status: automated evidence; human review is required before activation.

## Final targets

| Final file | Role | Result | Source coverage | Target coverage | Header |
|---|---|---|---:|---:|---|
| `TotalCrossSDK/src/main/java/tc/simulator/ApplicationLoader.java` | `primary` | `inherited`/high (direct) | 4.7% | 68.9% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/CommandLineParser.java` | `primary` | `inherited`/high (direct) | 1.2% | 11.8% | `incomplete` |
| `TotalCrossSDK/src/main/java/tc/simulator/FontRegistry.java` | `primary` | `inherited`/high (direct) | 9.4% | 98.5% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/FrameRenderer.java` | `primary` | `inherited`/high (direct) | 5.2% | 92.8% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/InputDispatcher.java` | `primary` | `inherited`/high (direct) | 14.2% | 82.9% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/Launcher.java` | `primary` | `inherited`/high (direct) | 2.1% | 21.7% | `incomplete` |
| `TotalCrossSDK/src/main/java/tc/simulator/RuntimeState.java` | `primary` | `inherited`/high (direct) | 4.0% | 67.4% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/SettingsBridge.java` | `primary` | `inherited`/high (direct) | 17.7% | 98.8% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/SimulatorCore.java` | `primary` | `inherited`/high (transitive) | 0.0% | 0.0% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/SimulatorSupport.java` | `primary` | `inherited`/high (direct) | 0.6% | 54.3% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/StorageBridge.java` | `primary` | `inherited`/high (direct) | 17.0% | 99.8% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/StreamBridge.java` | `primary` | `inherited`/high (direct) | 2.5% | 94.4% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/StreamTypes.java` | `primary` | `inherited`/high (direct) | 6.1% | 100.0% | `preserved` |
| `TotalCrossSDK/src/main/java/tc/simulator/awt/AwtWindow.java` | `primary` | `partial-inherited`/medium (direct) | 0.6% | 20.6% | `incomplete` |

## Findings

### `TotalCrossSDK/src/main/java/tc/simulator/ApplicationLoader.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 752/15881 (4.7%), target 752/1092 (68.9%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherArgumentParser.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherArguments.java` → `TotalCrossSDK/src/main/java/tc/simulator/ApplicationLoader.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherArgumentParser.java`
- `TotalCrossSDK/src/main/java/totalcross/LauncherArguments.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 414-450 `Launcher: static void showInstructions ( )` | 68-104 `ApplicationLoader: static void showInstructions ( )` | copied/high | 322 | 100.0% | 100.0% |
| 452-463 `Launcher: public static void main ( String args [ ] )` | 106-115 `ApplicationLoader: public static void main ( String args [ ] )` | copied-fragment/high | 75 | 81.5% | 88.0% |
| 465-472 `Launcher: private int toInt ( String s )` | 117-124 `ApplicationLoader: protected int toInt ( String s )` | copied/high | 29 | 96.7% | 96.7% |
| 482-484 `Launcher: protected void parseArguments ( String ... args )` | 126-128 `ApplicationLoader: protected void parseArguments ( String ... args )` | copied/high | 35 | 97.1% | 100.0% |
| 713-720 `Launcher: private String [ ] tokenizeString ( String string , char c )` | 160-167 `ApplicationLoader: protected String [ ] tokenizeString ( String string …` | copied/high | 81 | 98.8% | 98.8% |
| 728-730 `Launcher: public void registerMainWindow ( totalcross . ui . MainWindow…` | 200-202 `ApplicationLoader: public void registerMainWindow ( totalcross . ui . M…` | copied/high | 26 | 100.0% | 100.0% |
| 736-741 `Launcher: public void exit ( int exitCode )` | 208-213 `ApplicationLoader: public void exit ( int exitCode )` | copied/high | 26 | 100.0% | 100.0% |
| 767-789 `Launcher: private void updateModifiers ( java . awt . event . KeyEvent …` | 239-261 `ApplicationLoader: protected void updateModifiers ( java . awt . event …` | copied/high | 158 | 99.4% | 99.4% |

### `TotalCrossSDK/src/main/java/tc/simulator/CommandLineParser.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 193/15881 (1.2%), target 236/2000 (11.8%).
Header assessment: **incomplete**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherArgumentParser.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherArguments.java` → `TotalCrossSDK/src/main/java/tc/simulator/CommandLineParser.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherArgumentParser.java`
- `TotalCrossSDK/src/main/java/totalcross/LauncherArguments.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 486-711 `Launcher: protected void parseArguments ( String clazz , String ... arg…` | 186-192 `CommandLineParser: private static totalcross . ui . Insets parseInsets …` | copied-fragment/medium | 81 | 8.9% | 9.1% |
| 465-472 `Launcher: private int toInt ( String s )` | 218-224 `CommandLineParser: private static int toInt ( String s )` | copied/high | 30 | 98.4% | 98.4% |
| 713-720 `Launcher: private String [ ] tokenizeString ( String string , char c )` | 226-233 `CommandLineParser: private static String [ ] tokenizeString ( String st…` | copied/high | 82 | 99.4% | 99.4% |
| 486-711 `Launcher: protected void parseArguments ( String clazz , String ... arg…` | 239-245 `CommandLineParser: private static String fullCommandLine ( String [ ] a…` | copied-fragment/medium | 43 | 4.8% | 5.3% |

### `TotalCrossSDK/src/main/java/tc/simulator/FontRegistry.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 1495/15881 (9.4%), target 1495/1517 (98.5%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherFontTypes.java` → `TotalCrossSDK/src/main/java/tc/simulator/FontRegistry.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherFontTypes.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 465-472 `Launcher: private int toInt ( String s )` | 86-92 `FontRegistry: protected int toInt ( String value )` | adapted/high | 29 | 90.0% | 96.7% |
| 2201-2220 `Launcher.UserFont: private UserFont ( String fontName , String sufix , …` | 125-144 `FontRegistry.UserFont: protected UserFont ( String fontName , String su…` | copied/high | 222 | 99.6% | 99.6% |
| 2222-2299 `Launcher.UserFont: private UserFont ( String fontName , String sufix ) …` | 146-223 `FontRegistry.UserFont: protected UserFont ( String fontName , String su…` | copied/high | 687 | 99.9% | 99.9% |
| 2301-2316 `Launcher.UserFont: private totalcross . ui . image . Image getBaseCharI…` | 225-240 `FontRegistry.UserFont: protected totalcross . ui . image . Image getBas…` | copied/high | 158 | 99.4% | 99.4% |
| 2319-2352 `Launcher.UserFont: public void setCharBits ( char ch , CharBits bits )` | 243-276 `FontRegistry.UserFont: public void setCharBits ( char ch , CharBits bit…` | copied/high | 240 | 100.0% | 100.0% |
| 2355-2370 `Launcher: public int getCharWidth ( totalcross . ui . font . Font f , c…` | 279-294 `FontRegistry: public int getCharWidth ( totalcross . ui . font . Font f…` | copied/high | 159 | 100.0% | 100.0% |

### `TotalCrossSDK/src/main/java/tc/simulator/FrameRenderer.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 831/15881 (5.2%), target 831/895 (92.8%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherRendering.java` → `TotalCrossSDK/src/main/java/tc/simulator/FrameRenderer.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherRendering.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 1164-1168 `Launcher: public static void print ( String s )` | 72-76 `FrameRenderer: public static void print ( String s )` | copied/high | 31 | 100.0% | 100.0% |
| 1169-1173 `Launcher: public static void debug ( String s )` | 77-81 `FrameRenderer: public static void debug ( String s )` | copied/high | 31 | 100.0% | 100.0% |
| 1177-1203 `Launcher: private void createColorPaletteLookupTables ( )` | 85-111 `FrameRenderer: protected void createColorPaletteLookupTables ( )` | copied/high | 181 | 99.5% | 99.5% |
| 1205-1220 `Launcher: private int getScreenColor ( int p )` | 113-128 `FrameRenderer: protected int getScreenColor ( int p )` | copied/high | 139 | 99.3% | 99.3% |
| 1222-1316 `Launcher: public void updateScreen ( )` | 130-187 `FrameRenderer: public void updateScreen ( )` | copied-fragment/medium | 365 | 66.1% | 69.1% |
| 1318-1331 `Launcher: public static BufferedImage toBufferedImage ( java . awt . Im…` | 196-209 `FrameRenderer: public static BufferedImage toBufferedImage ( java . awt…` | copied/high | 84 | 100.0% | 100.0% |

### `TotalCrossSDK/src/main/java/tc/simulator/InputDispatcher.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 2251/15881 (14.2%), target 2286/2758 (82.9%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherInput.java` → `TotalCrossSDK/src/main/java/tc/simulator/InputDispatcher.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherInput.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 959-981 `Launcher: @ Override public void keyTyped ( java . awt . event . KeyEve…` | 75-79 `InputDispatcher: void injectPreviewKey ( int keyCode , boolean pressed …` | adapted-fragment/medium | 35 | 37.4% | 49.2% |
| 791-905 `Launcher: @ Override public void keyPressed ( final java . awt . event …` | 81-195 `InputDispatcher: @ Override public void keyPressed ( final java . awt .…` | copied/high | 738 | 99.3% | 100.0% |
| 907-918 `Launcher: private void takeScreenShot ( )` | 197-208 `InputDispatcher: protected void takeScreenShot ( )` | copied/high | 108 | 99.1% | 99.1% |
| 920-929 `Launcher: private void screenResized ( int w , int h , boolean setframe…` | 210-219 `InputDispatcher: protected void screenResized ( int w , int h , boolean…` | copied/high | 85 | 94.4% | 97.8% |
| 931-938 `Launcher: @ Override public void transferFocus ( )` | 221-228 `InputDispatcher: @ Override public void transferFocus ( )` | copied/high | 48 | 93.8% | 100.0% |
| 940-957 `Launcher: @ Override public void keyReleased ( java . awt . event . Key…` | 230-247 `InputDispatcher: @ Override public void keyReleased ( java . awt . even…` | copied/high | 90 | 100.0% | 100.0% |
| 959-981 `Launcher: @ Override public void keyTyped ( java . awt . event . KeyEve…` | 249-271 `InputDispatcher: @ Override public void keyTyped ( java . awt . event .…` | copied/high | 131 | 97.7% | 100.0% |
| 986-996 `Launcher: @ Override public void mousePressed ( java . awt . event . Mo…` | 276-286 `InputDispatcher: @ Override public void mousePressed ( java . awt . eve…` | copied/high | 123 | 95.9% | 100.0% |
| 998-1008 `Launcher: @ Override public void mouseReleased ( java . awt . event . M…` | 288-298 `InputDispatcher: @ Override public void mouseReleased ( java . awt . ev…` | copied/high | 119 | 95.8% | 100.0% |
| 1010-1028 `Launcher: @ Override public void mouseDragged ( java . awt . event . Mo…` | 300-318 `InputDispatcher: @ Override public void mouseDragged ( java . awt . eve…` | copied/high | 191 | 95.3% | 100.0% |
| 1030-1043 `Launcher: @ Override public void mouseWheelMoved ( MouseWheelEvent e )` | 320-333 `InputDispatcher: @ Override public void mouseWheelMoved ( MouseWheelEve…` | copied/high | 123 | 95.9% | 100.0% |
| 1045-1054 `Launcher: @ Override public void windowClosing ( java . awt . event . W…` | 335-344 `InputDispatcher: @ Override public void windowClosing ( java . awt . ev…` | copied/high | 72 | 97.2% | 100.0% |
| 1056-1061 `Launcher: @ Override public void mouseEntered ( java . awt . event . Mo…` | 346-351 `InputDispatcher: @ Override public void mouseEntered ( java . awt . eve…` | copied-fragment/medium | 31 | 81.6% | 89.5% |
| 1083-1088 `Launcher: @ Override public void windowDeiconified ( java . awt . event…` | 373-378 `InputDispatcher: @ Override public void windowDeiconified ( java . awt …` | copied/high | 31 | 100.0% | 100.0% |
| 1090-1095 `Launcher: @ Override public void windowIconified ( java . awt . event .…` | 380-385 `InputDispatcher: @ Override public void windowIconified ( java . awt . …` | copied/high | 31 | 100.0% | 100.0% |
| 1101-1123 `Launcher: @ Override public void mouseMoved ( java . awt . event . Mous…` | 391-413 `InputDispatcher: @ Override public void mouseMoved ( java . awt . event…` | copied/high | 226 | 96.6% | 98.7% |
| 1125-1142 `Launcher: @ Override public void paint ( java . awt . Graphics g )` | 415-432 `InputDispatcher: @ Override public void paint ( java . awt . Graphics g…` | copied/high | 83 | 96.5% | 98.8% |
| 1144-1148 `Launcher: public void pumpEvents ( )` | 434-438 `InputDispatcher: public void pumpEvents ( )` | adapted/high | 21 | 85.7% | 100.0% |

### `TotalCrossSDK/src/main/java/tc/simulator/Launcher.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 334/15881 (2.1%), target 394/1819 (21.7%).
Header assessment: **incomplete**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherRuntime.java` → `TotalCrossSDK/src/main/java/tc/simulator/Launcher.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherRuntime.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 2201-2220 `Launcher.UserFont: private UserFont ( String fontName , String sufix , …` | 30-32 `Launcher.UserFont: protected UserFont ( String fontName , String suffix…` | adapted-fragment/medium | 26 | 20.1% | 21.6% |
| 1144-1148 `Launcher: public void pumpEvents ( )` | 125-129 `Launcher: public void pumpEvents ( )` | adapted/high | 21 | 90.5% | 100.0% |
| 1144-1148 `Launcher: public void pumpEvents ( )` | 188-193 `Launcher: public void stop ( )` | adapted/medium | 19 | 73.9% | 91.3% |
| 186-273 `Launcher: @ Override @ SuppressWarnings ( STRING:904f07a3e3 ) final pub…` | 253-271 `Launcher: public MainWindow createMainWindow ( String className , Class…` | adapted-fragment/medium | 96 | 31.9% | 38.9% |
| 186-273 `Launcher: @ Override @ SuppressWarnings ( STRING:904f07a3e3 ) final pub…` | 302-307 `Launcher: static String normalizeMainWindowClassName ( String className…` | copied-fragment/medium | 41 | 16.0% | 17.5% |
| 275-285 `Launcher: private static boolean checkIfMainClass ( Class < ? > c )` | 355-365 `Launcher: private static boolean checkIfMainClass ( Class < ? > c )` | copied/high | 77 | 100.0% | 100.0% |
| 172-184 `Launcher: private void runtimeInstructions ( )` | 367-378 `Launcher: private static void runtimeInstructions ( )` | copied/high | 114 | 99.6% | 99.6% |

### `TotalCrossSDK/src/main/java/tc/simulator/RuntimeState.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 638/15881 (4.0%), target 638/946 (67.4%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherState.java` → `TotalCrossSDK/src/main/java/tc/simulator/RuntimeState.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherState.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 155-170 `Launcher: @ Override public void destroy ( )` | 68-92 `RuntimeState: public void destroy ( )` | copied-fragment/medium | 63 | 64.6% | 67.7% |
| 186-273 `Launcher: @ Override @ SuppressWarnings ( STRING:904f07a3e3 ) final pub…` | 94-165 `RuntimeState: @ SuppressWarnings ( STRING:904f07a3e3 ) final public voi…` | copied-fragment/medium | 332 | 78.5% | 77.8% |
| 329-373 `Launcher.WinTimer: @ Override public void run ( )` | 235-279 `RuntimeState.WinTimer: @ Override public void run ( )` | copied/high | 137 | 97.2% | 99.3% |
| 375-379 `Launcher.WinTimer: void setInterval ( int millis )` | 281-285 `RuntimeState.WinTimer: void setInterval ( int millis )` | copied/high | 22 | 100.0% | 100.0% |
| 393-412 `Launcher: void startApp ( )` | 299-318 `RuntimeState: void startApp ( )` | copied/high | 84 | 94.4% | 98.9% |

### `TotalCrossSDK/src/main/java/tc/simulator/SettingsBridge.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 2817/15881 (17.7%), target 2817/2850 (98.8%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherSettings.java` → `TotalCrossSDK/src/main/java/tc/simulator/SettingsBridge.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherSettings.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 1800-1823 `Launcher: String getDefaultCrid ( String name )` | 68-91 `SettingsBridge: String getDefaultCrid ( String name )` | copied/high | 173 | 100.0% | 100.0% |
| 1825-1874 `Launcher: void storeSettings ( )` | 93-142 `SettingsBridge: protected void storeSettings ( )` | copied/high | 363 | 99.9% | 99.9% |
| 1876-1910 `Launcher: private void getAppSettings ( )` | 144-178 `SettingsBridge: protected void getAppSettings ( )` | copied/high | 249 | 99.6% | 99.6% |
| 1912-1920 `Launcher: private char getFirstSymbol ( String s )` | 180-188 `SettingsBridge: protected char getFirstSymbol ( String s )` | copied/high | 74 | 98.7% | 98.7% |
| 1923-1987 `Launcher: public void fillSettings ( )` | 191-255 `SettingsBridge: public void fillSettings ( )` | copied/high | 641 | 100.0% | 100.0% |
| 1989-2004 `Launcher: @ SuppressWarnings ( STRING:9955ddedef ) public void settings…` | 257-272 `SettingsBridge: @ SuppressWarnings ( STRING:9955ddedef ) public void se…` | copied/high | 119 | 100.0% | 100.0% |
| 2014-2044 `Launcher: static totalcross . ui . font . Font getBaseFont ( String nam…` | 274-304 `SettingsBridge: static totalcross . ui . font . Font getBaseFont ( Stri…` | copied/high | 270 | 100.0% | 100.0% |
| 2046-2075 `Launcher: private UserFont loadUF ( String fontName , String suffix )` | 306-335 `SettingsBridge: protected Launcher . UserFont loadUF ( String fontName …` | copied/high | 242 | 95.1% | 95.1% |
| 2077-2158 `Launcher: public UserFont getFont ( totalcross . ui . font . Font f , c…` | 337-418 `SettingsBridge: public Launcher . UserFont getFont ( totalcross . ui . …` | copied/high | 686 | 99.6% | 99.6% |

### `TotalCrossSDK/src/main/java/tc/simulator/SimulatorCore.java`

Classification: **inherited** (high, transitive evidence; assignment `primary`).
Direct matched tokens: source 0/15881 (0.0%), target 0/126 (0.0%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/tc/simulator/SimulatorCore.java`

### `TotalCrossSDK/src/main/java/tc/simulator/SimulatorSupport.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 102/15881 (0.6%), target 102/188 (54.3%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherSupport.java` → `TotalCrossSDK/src/main/java/tc/simulator/SimulatorSupport.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherSupport.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 137-153 `Launcher: public Launcher ( )` | 123-140 `SimulatorSupport: protected void initializeLauncher ( )` | copied-fragment/high | 102 | 92.7% | 94.5% |

### `TotalCrossSDK/src/main/java/tc/simulator/StorageBridge.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 2699/15881 (17.0%), target 2699/2705 (99.8%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherStorage.java` → `TotalCrossSDK/src/main/java/tc/simulator/StorageBridge.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherStorage.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 1335-1369 `Launcher: private File [ ] getClassPathDirectories ( ) throws Exception` | 69-103 `StorageBridge: protected File [ ] getClassPathDirectories ( ) throws Ex…` | copied/high | 234 | 99.6% | 99.6% |
| 1371-1391 `Launcher: private InputStream readJavaInputStream ( java . io . InputSt…` | 105-125 `StorageBridge: protected InputStream readJavaInputStream ( java . io . …` | copied/high | 112 | 99.1% | 99.1% |
| 1393-1401 `Launcher: private String getPathOf ( String pathAndFileName )` | 127-135 `StorageBridge: protected String getPathOf ( String pathAndFileName )` | copied/high | 71 | 98.6% | 98.6% |
| 1403-1417 `Launcher: public String getDataPath ( )` | 137-151 `StorageBridge: public String getDataPath ( )` | copied/high | 56 | 100.0% | 100.0% |
| 1419-1425 `Launcher: private String getMainWindowPath ( )` | 153-159 `StorageBridge: protected String getMainWindowPath ( )` | copied/high | 53 | 98.1% | 98.1% |
| 1428-1629 `Launcher: public InputStream openInputStream ( String path )` | 162-363 `StorageBridge: public InputStream openInputStream ( String path )` | copied/high | 1305 | 99.8% | 100.0% |
| 1631-1647 `Launcher: private OutputStream openOutputUrl ( URL url )` | 365-381 `StorageBridge: protected OutputStream openOutputUrl ( URL url )` | copied/high | 101 | 99.0% | 99.0% |
| 1650-1752 `Launcher: public OutputStream openOutputStream ( String path )` | 384-486 `StorageBridge: public OutputStream openOutputStream ( String path )` | copied/high | 569 | 99.8% | 100.0% |
| 1757-1771 `Launcher: public byte [ ] readBytes ( String path )` | 491-505 `StorageBridge: public byte [ ] readBytes ( String path )` | copied/high | 83 | 100.0% | 100.0% |
| 1776-1793 `Launcher: public boolean writeBytes ( String path , byte [ ] buf , int …` | 510-527 `StorageBridge: public boolean writeBytes ( String path , byte [ ] buf ,…` | copied/high | 94 | 100.0% | 100.0% |
| 1796-1798 `Launcher: private boolean isOk ( String s )` | 530-532 `StorageBridge: protected boolean isOk ( String s )` | copied/high | 21 | 95.5% | 95.5% |

### `TotalCrossSDK/src/main/java/tc/simulator/StreamBridge.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 390/15881 (2.5%), target 390/413 (94.4%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherStreams.java` → `TotalCrossSDK/src/main/java/tc/simulator/StreamBridge.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherStreams.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 2402-2415 `Launcher: public void alert ( String msg )` | 68-81 `StreamBridge: public void alert ( String msg )` | copied/high | 71 | 100.0% | 100.0% |
| 2627-2634 `Launcher: public void setTitle ( String title )` | 84-91 `StreamBridge: public void setTitle ( String title )` | copied-fragment/medium | 28 | 84.8% | 90.9% |
| 2636-2665 `Launcher: public void vibrate ( final int millis )` | 93-122 `StreamBridge: public void vibrate ( final int millis )` | copied/high | 203 | 95.8% | 97.6% |
| 2684-2699 `Launcher: @ Override public void componentResized ( ComponentEvent ev )` | 141-159 `StreamBridge: @ Override public void componentResized ( ComponentEvent …` | copied-fragment/medium | 88 | 80.4% | 82.2% |

### `TotalCrossSDK/src/main/java/tc/simulator/StreamTypes.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 972/15881 (6.1%), target 1024/1024 (100.0%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/LauncherStreamTypes.java` → `TotalCrossSDK/src/main/java/tc/simulator/StreamTypes.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/LauncherStreamTypes.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 2376-2388 `Launcher.AlertBox: public AlertBox ( )` | 73-85 `StreamTypes.AlertBox: public AlertBox ( )` | copied/high | 114 | 100.0% | 100.0% |
| 2390-2395 `Launcher.AlertBox: @ Override public void actionPerformed ( java . awt …` | 87-92 `StreamTypes.AlertBox: @ Override public void actionPerformed ( java . a…` | copied/high | 34 | 100.0% | 100.0% |
| 2425-2432 `Launcher.IS2S: @ Override public void close ( )` | 106-113 `StreamTypes.IS2S: @ Override public void close ( )` | copied/high | 29 | 100.0% | 100.0% |
| 2434-2441 `Launcher.IS2S: @ Override public int readBytes ( byte [ ] buf , int sta…` | 115-122 `StreamTypes.IS2S: @ Override public int readBytes ( byte [ ] buf , int …` | copied/high | 45 | 100.0% | 100.0% |
| 2443-2446 `Launcher.IS2S: @ Override public int writeBytes ( byte [ ] buf , int st…` | 124-127 `StreamTypes.IS2S: @ Override public int writeBytes ( byte [ ] buf , int…` | copied/high | 22 | 100.0% | 100.0% |
| 2454-2456 `Launcher.S2FIS: public S2FIS ( RandomAccessStream s )` | 135-137 `StreamTypes.S2FIS: public S2FIS ( RandomAccessStream s )` | copied/high | 18 | 100.0% | 100.0% |
| 2458-2460 `Launcher.S2FIS: public S2FIS ( RandomAccessStream s , int max )` | 139-141 `StreamTypes.S2FIS: public S2FIS ( RandomAccessStream s , int max )` | copied/high | 20 | 100.0% | 100.0% |
| 2462-2465 `Launcher.S2FIS: public S2FIS ( RandomAccessStream s , int max , boolean…` | 143-146 `StreamTypes.S2FIS: public S2FIS ( RandomAccessStream s , int max , bool…` | copied/high | 33 | 100.0% | 100.0% |
| 2467-2475 `Launcher.S2FIS: @ Override public synchronized void mark ( int readlimi…` | 148-156 `StreamTypes.S2FIS: @ Override public synchronized void mark ( int readl…` | copied/high | 44 | 100.0% | 100.0% |
| 2477-2486 `Launcher.S2FIS: @ Override public synchronized void reset ( ) throws ja…` | 158-167 `StreamTypes.S2FIS: @ Override public synchronized void reset ( ) throws…` | copied/high | 74 | 100.0% | 100.0% |
| 2500-2502 `Launcher.S2IS: public S2IS ( Stream s )` | 181-183 `StreamTypes.S2IS: public S2IS ( Stream s )` | copied/high | 18 | 100.0% | 100.0% |
| 2504-2506 `Launcher.S2IS: public S2IS ( Stream s , int max )` | 185-187 `StreamTypes.S2IS: public S2IS ( Stream s , int max )` | copied/high | 20 | 100.0% | 100.0% |
| 2508-2512 `Launcher.S2IS: public S2IS ( Stream s , int max , boolean closeUnderlyi…` | 189-193 `StreamTypes.S2IS: public S2IS ( Stream s , int max , boolean closeUnder…` | copied/high | 32 | 100.0% | 100.0% |
| 2514-2531 `Launcher.S2IS: @ Override public int read ( ) throws java . io . IOExce…` | 195-212 `StreamTypes.S2IS: @ Override public int read ( ) throws java . io . IOE…` | copied/high | 102 | 100.0% | 100.0% |
| 2533-2554 `Launcher.S2IS: @ Override public int read ( byte [ ] buf , int off , in…` | 214-235 `StreamTypes.S2IS: @ Override public int read ( byte [ ] buf , int off ,…` | copied/high | 113 | 100.0% | 100.0% |
| 2556-2565 `Launcher.S2IS: @ Override public void close ( ) throws java . io . IOEx…` | 237-246 `StreamTypes.S2IS: @ Override public void close ( ) throws java . io . I…` | copied/high | 52 | 100.0% | 100.0% |
| 2578-2581 `Launcher.S2OS: public S2OS ( Stream s , boolean closeUnderlying )` | 259-262 `StreamTypes.S2OS: public S2OS ( Stream s , boolean closeUnderlying )` | copied/high | 23 | 100.0% | 100.0% |
| 2587-2600 `Launcher.S2OS: @ Override public void write ( int b ) throws java . io …` | 268-281 `StreamTypes.S2OS: @ Override public void write ( int b ) throws java . …` | copied/high | 92 | 100.0% | 100.0% |
| 2602-2613 `Launcher.S2OS: @ Override public void write ( byte [ ] b , int off , in…` | 283-294 `StreamTypes.S2OS: @ Override public void write ( byte [ ] b , int off ,…` | copied/high | 87 | 100.0% | 100.0% |
| 2556-2565 `Launcher.S2IS: @ Override public void close ( ) throws java . io . IOEx…` | 296-305 `StreamTypes.S2OS: @ Override public void close ( ) throws java . io . I…` | copied/high | 52 | 100.0% | 100.0% |

### `TotalCrossSDK/src/main/java/tc/simulator/awt/AwtWindow.java`

Classification: **partial-inherited** (medium, direct evidence; assignment `primary`).
Direct matched tokens: source 97/15881 (0.6%), target 97/472 (20.6%).
Header assessment: **incomplete**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/Launcher.java` → `TotalCrossSDK/src/main/java/totalcross/preview/AwtWindowBackend.java` → `TotalCrossSDK/src/main/java/tc/simulator/awt/AwtWindow.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/main/java/totalcross/preview/AwtWindowBackend.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 2508-2512 `Launcher.S2IS: public S2IS ( Stream s , int max , boolean closeUnderlyi…` | 33-37 `AwtWindow: public AwtWindow ( Frame frame , Component component , Rende…` | adapted/medium | 22 | 59.4% | 93.8% |
| 316-322 `Launcher.LauncherFrame: public void setFrameSize ( int toWidth , int to…` | 89-95 `AwtWindow: public void setContentSize ( int width , int height , boolea…` | adapted/high | 75 | 83.3% | 98.9% |

## Interpretation

- `inherited`: strong material lineage.
- `partial-inherited`: a material extracted or adapted portion was detected.
- `manual-review`: multiple non-generic code identifiers moved into a newly created production file, without enough textual evidence for an automatic inheritance decision.
- `manual-review` edges never support transitive inherited classifications.
- Intermediate files document the path but receive no final decision if removed.
- This is technical provenance evidence, not an independent legal opinion.
