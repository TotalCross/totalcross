// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.qrcode;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * This class provides generation of QR code that can represent BINARY,
 * NUMERICAL and ALPHANUMERICAL data. See the example bellow:
 * <pre>
 *     Image image = new QRCode.generate("John Doe");
 * </pre>
 */
public class QRCode {

    /**
     * This variable represents low level error correction
     */
    public static final int ECC_LOW = 0;
    /**
     * This variable represents medium level error correction
     */
    public static final int ECC_MEDIUM = 1;
    /**
     * This variable represents quartile level error correction
     */
    public static final int ECC_QUARTILE = 2;
    /**
     * This variable represents high level error correction
     */
    public static final int ECC_HIGH = 3;

    private static final int DATA_BITS = 0;
    private static final int NUMERIC = 1;
    private static final int ALPHA_NUMERIC = 2;
    private static final int BINARY = 3;
    private static final int KANJI = 4;
    
    public static boolean DEBUG = false;
    /**
     * Used to find the most appropriate version considering type of date
     * first column => Data bits (mixed)
     * second column => Numeric
     * third column => Alphanumeric
     * fourth column => Binary
     * fiveth column => Kanji
     */
    private static final int[][] lowQualityParameters =
            {
                    {152, 41, 25, 17, 10},
                    {272, 77, 47, 32, 20},
                    {440, 127, 77, 53, 32},
                    {640, 187, 114, 78, 48},
                    {864, 255, 154, 106, 65},
                    {1088, 322, 195, 134, 82},
                    {1248, 370, 224, 154, 95},
                    {1552, 461, 279, 192, 118},
                    {1856, 552, 335, 230, 141},
                    {2192, 652, 395, 271, 167},
                    {2592, 772, 468, 321, 198},
                    {2960, 883, 535, 367, 226},
                    {3424, 1022, 619, 425, 262},
                    {3688, 1101, 667, 458, 282},
                    {4184, 1250, 758, 520, 320},
                    {4712, 1408, 854, 586, 361},
                    {5176, 1548, 938, 644, 397},
                    {5768, 1725, 1046, 718, 442},
                    {6360, 1903, 1153, 792, 488},
                    {6888, 2061, 1249, 858, 528},
                    {7456, 2232, 1352, 929, 572},
                    {8048, 2409, 1460, 1003, 618},
                    {8752, 2620, 1588, 1091, 672},
                    {9392, 2812, 1704, 1171, 721},
                    {10208, 3057, 1853, 1273, 784},
                    {10960, 3283, 1990, 1367, 842},
                    {11744, 3517, 2132, 1465, 902},
                    {12248, 3669, 2223, 1528, 940},
                    {13048, 3909, 2369, 1628, 1002},
                    {13880, 4158, 2520, 1732, 1066},
                    {14744, 4417, 2677, 1840, 1132},
                    {15640, 4686, 2840, 1952, 1201},
                    {16568, 4965, 3009, 2068, 1273},
                    {17528, 5253, 3183, 2188, 1347},
                    {18448, 5529, 3351, 2303, 1417},
                    {19472, 5836, 3537, 2431, 1496},
                    {20528, 6153, 3729, 2563, 1577},
                    {21616, 6479, 3927, 2699, 1661},
                    {22496, 6743, 4087, 2809, 1729},
                    {23648, 7089, 4296, 2953, 1817},

            };

    /**
     * Used to find the most appropriate version considering type of date
     * first column => Data bits (mixed)
     * second column => Numeric
     * third column => Alphanumeric
     * fourth column => Binary
     * fiveth column => Kanji
     */
    private static final int[][] mediumQualityParameters = {
            {128, 34, 20, 14, 8},
            {224, 63, 38, 26, 16},
            {352, 101, 61, 42, 26},
            {512, 149, 90, 62, 38},
            {688, 202, 122, 84, 52},
            {864, 255, 154, 106, 65},
            {992, 293, 178, 122, 75},
            {1232, 365, 221, 152, 93},
            {1456, 432, 262, 180, 111},
            {1728, 513, 311, 213, 131},
            {2032, 604, 366, 251, 155},
            {2320, 691, 419, 287, 177},
            {2672, 796, 483, 331, 204},
            {2920, 871, 528, 362, 223},
            {3320, 991, 600, 412, 254},
            {3624, 1082, 656, 450, 277},
            {4056, 1212, 734, 504, 310},
            {4504, 1346, 816, 560, 345},
            {5016, 1500, 909, 624, 384},
            {5352, 1600, 970, 666, 410},
            {5712, 1708, 1035, 711, 438},
            {6256, 1872, 1134, 779, 480},
            {6880, 2059, 1248, 857, 528},
            {7312, 2188, 1326, 911, 561},
            {8000, 2395, 1451, 997, 614},
            {8496, 2544, 1542, 1059, 652},
            {9024, 2701, 1637, 1125, 692},
            {9544, 2857, 1732, 1190, 732},
            {10136, 3035, 1839, 1264, 778},
            {10984, 3289, 1994, 1370, 843},
            {11640, 3486, 2113, 1452, 894},
            {12328, 3693, 2238, 1538, 947},
            {13048, 3909, 2369, 1628, 1002},
            {13800, 4134, 2506, 1722, 1060},
            {14496, 4343, 2632, 1809, 1113},
            {15312, 4588, 2780, 1911, 1176},
            {15936, 4775, 2894, 1989, 1224},
            {16816, 5039, 3054, 2099, 1292},
            {17728, 5313, 3220, 2213, 1362},
            {18672, 5596, 3391, 2331, 1435},
    };

    /**
     * Used to find the most appropriate version considering type of date
     * first column => Data bits (mixed)
     * second column => Numeric
     * third column => Alphanumeric
     * fourth column => Binary
     * fiveth column => Kanji
     */
    private static final int[][] quartilyQualityParameters = {
            {104, 27, 16, 11, 7},
            {176, 48, 29, 20, 12},
            {272, 77, 47, 32, 20},
            {384, 111, 67, 46, 28},
            {496, 144, 87, 60, 37},
            {608, 178, 108, 74, 45},
            {704, 207, 125, 86, 53},
            {880, 259, 157, 108, 66},
            {1056, 312, 189, 130, 80},
            {1232, 364, 221, 151, 93},
            {1440, 427, 259, 177, 109},
            {1648, 489, 296, 203, 125},
            {1952, 580, 352, 241, 149},
            {2088, 621, 376, 258, 159},
            {2360, 703, 426, 292, 180},
            {2600, 775, 470, 322, 198},
            {2936, 876, 531, 364, 224},
            {3176, 948, 574, 394, 243},
            {3560, 1063, 644, 442, 272},
            {3880, 1159, 702, 482, 297},
            {4096, 1224, 742, 509, 314},
            {4544, 1358, 823, 565, 348},
            {4912, 1468, 890, 611, 376},
            {5312, 1588, 963, 661, 407},
            {5744, 1718, 1041, 715, 440},
            {6032, 1804, 1094, 751, 462},
            {6464, 1933, 1172, 805, 496},
            {6968, 2085, 1263, 868, 534},
            {7288, 2181, 1322, 908, 559},
            {7880, 2358, 1429, 982, 604},
            {8264, 2473, 1499, 1030, 634},
            {8920, 2670, 1618, 1112, 684},
            {9368, 2805, 1700, 1168, 719},
            {9848, 2949, 1787, 1228, 756},
            {10288, 3081, 1867, 1283, 790},
            {10832, 3244, 1966, 1351, 832},
            {11408, 3417, 2071, 1423, 876},
            {12016, 3599, 2181, 1499, 923},
            {12656, 3791, 2298, 1579, 972},
            {13328, 3993, 2420, 1663, 1024},

    };
    
    /**
     * Used to find the most appropriate version considering type of date
     * first column => Data bits (mixed)
     * second column => Numeric
     * third column => Alphanumeric
     * fourth column => Binary
     * fiveth column => Kanji
     */
    private static final int[][] highQualityParameters = {
            {72, 17, 10, 7, 4},
            {128, 34, 20, 14, 8},
            {208, 58, 35, 24, 15},
            {288, 82, 50, 34, 21},
            {368, 106, 64, 44, 27},
            {480, 139, 84, 58, 36},
            {528, 154, 93, 64, 39},
            {688, 202, 122, 84, 52},
            {800, 235, 143, 98, 60},
            {976, 288, 174, 119, 74},
            {1120, 331, 200, 137, 85},
            {1264, 374, 227, 155, 96},
            {1440, 427, 259, 177, 109},
            {1576, 468, 283, 194, 120},
            {1784, 530, 321, 220, 136},
            {2024, 602, 365, 250, 154},
            {2264, 674, 408, 280, 173},
            {2504, 746, 452, 310, 191},
            {2728, 813, 493, 338, 208},
            {3080, 919, 557, 382, 235},
            {3248, 969, 587, 403, 248},
            {3536, 1056, 640, 439, 270},
            {3712, 1108, 672, 461, 284},
            {4112, 1228, 744, 511, 315},
            {4304, 1286, 779, 535, 330},
            {4768, 1425, 864, 593, 365},
            {5024, 1501, 910, 625, 385},
            {5288, 1581, 958, 658, 405},
            {5608, 1677, 1016, 698, 430},
            {5960, 1782, 1080, 742, 457},
            {6344, 1897, 1150, 790, 486},
            {6760, 2022, 1226, 842, 518},
            {7208, 2157, 1307, 898, 553},
            {7688, 2301, 1394, 958, 590},
            {7888, 2361, 1431, 983, 605},
            {8432, 2524, 1530, 1051, 647},
            {8768, 2625, 1591, 1093, 673},
            {9136, 2735, 1658, 1139, 701},
            {9776, 2927, 1774, 1219, 750},
            {10208, 3057, 1852, 1273, 784},

    };
    
    private static final int[][][] qualityParameters = {
	    lowQualityParameters,
	    mediumQualityParameters,
	    quartilyQualityParameters,
	    highQualityParameters
    };

    /**
     * This method returns the byte matrix that contains the values of each point of
     * the QRCode. A QR code is composed of many little squares, called modules,
     * which represent encoded data, with additional error correction (allowing
     * partially damaged QR codes to still be read). Blank modules are 0x00 and
     * black modules are 0xFF.
     *
     * @param version         The version of a QR code is a number between 1 and 40
     *                        (inclusive), which indicates the size of the QR code.
     *                        The width and height of a QR code are always equal (it
     *                        is square) and are equal to 4 * version + 17.
     * @param errorCorrection The level of error correction is a number between 0
     *                        and 3 (inclusive), or can be one of the symbolic names
     *                        ECC_LOW, ECC_MEDIUM, ECC_QUARTILE and ECC_HIGH. Higher
     *                        levels of error correction sacrifice data capacity,
     *                        but allow a larger portion of the QR code to be
     *                        damaged or unreadable.
     * @param text            The text to encode.
     */
    public byte[][] getBytes(String text, int errorCorrection, int version) {
	if (version < 1 || version > 40) {
	    throw new IllegalArgumentException("Version must be in [1,40].");
	}
	if (errorCorrection < ECC_LOW || errorCorrection > ECC_HIGH) {
	    throw new IllegalArgumentException(
		    "errorCorrection must be greater or equal QRCODE.ECC_LOW or less or equal QRCODE.ECC_HIGH");
	}
	if (text == null) {
	    throw new NullPointerException("Argument text cannot be null");
	}
	if (DEBUG) {
	    System.out.println("input: " + text);
	    System.out.println("text type:" + getDataType(text));
	    System.out.println("ecc: " + errorCorrection);
	    System.out.println("version: " + version);
	}

	return nativeGetBytes(text, errorCorrection, version);
    }

    @ReplacedByNativeOnDeploy
    private final byte[][] nativeGetBytes(String text, int errorCorrection, int version) {
	return new byte[10][10];
    }

    /**
     * This method returns the byte matrix that contains the values of each point of
     * the QRCode. A QR code is composed of many little squares, called modules,
     * which represent encoded data, with additional error correction (allowing
     * partially damaged QR codes to still be read). Blank modules are 0x00 and
     * black modules are 0xFF.
     * <p>
     * This method do some calculus to find the best version considering ecc and the
     * given text
     *
     * @param errorCorrection The level of error correction is a number between 0
     *                        and 3 (inclusive), or can be one of the symbolic names
     *                        ECC_LOW, ECC_MEDIUM, ECC_QUARTILE and ECC_HIGH. Higher
     *                        levels of error correction sacrifice data capacity,
     *                        but allow a larger portion of the QR code to be
     *                        damaged or unreadable.
     * @param text            The text to encode.
     */
    public byte[][] getBytes(String text, int errorCorrection) {
	return getBytes(text, errorCorrection, getBestVersion(text, errorCorrection));
    }

    /**
     * This method returns the byte matrix that contains the values of each point of
     * the QRCode. A QR code is composed of many little squares, called modules,
     * which represent encoded data, with additional error correction (allowing
     * partially damaged QR codes to still be read). Blank modules are 0x00 and
     * black modules are 0xFF.
     * <p>
     * This method do some calculus to find the best version considering ecc and the
     * given text.
     * <p>
     * By default this method uses {@link #ECC_QUARTILE} as default ecc.
     *
     * @param text The text to encode.
     */

    public byte[][] getBytes(String text) {
	return getBytes(text, ECC_QUARTILE);
    }

    /**
     * This method returns an totalcross.ui.Image instance that contains the values
     * of each point of the QRCode. A QR code is composed of many little squares,
     * called modules, which represent encoded data, with additional error
     * correction (allowing partially damaged QR codes to still be read). Blank
     * modules are 0x00 and black modules are 0xFF.
     *
     * @param text The text to encode.
     * @throws ImageException
     */
    public Image getImage(String text, int ecc, int version) throws ImageException {
	byte[][] b = getBytes(text, ecc, version);

	Image image = new Image(b.length, b[0].length);
	for (int i = 0; i < image.getHeight(); i++) {
	    for (int j = 0; j < image.getWidth(); j++) {
		Graphics g = image.getGraphics();
		if (b[i][j] == -1) {
		    g.foreColor = Color.BLACK;
		} else {
		    g.foreColor = Color.WHITE;
		}
		g.setPixel(i, j);
	    }
	}
	return image;
    }

    /**
     * This method returns an totalcross.ui.Image instance that contains the values
     * of each point of the QRCode. A QR code is composed of many little squares,
     * called modules, which represent encoded data, with additional error
     * correction (allowing partially damaged QR codes to still be read). Blank
     * modules are 0x00 and black modules are 0xFF.
     * <p>
     * This method do some calculus to find the best version considering ecc and the
     * given text.
     *
     * @param text The text to encode.
     * @param ecc  The correction level.
     * @throws ImageException
     */
    public Image getImage(String text, int ecc) throws ImageException {
	return getImage(text, ecc, getBestVersion(text, ecc));
    }

    /**
     * This method returns an totalcross.ui.Image instance that contains the values
     * of each point of the QRCode. A QR code is composed of many little squares,
     * called modules, which represent encoded data, with additional error
     * correction (allowing partially damaged QR codes to still be read). Blank
     * modules are 0x00 and black modules are 0xFF.
     * <p>
     * This method do some calculus to find the best version considering ecc and the
     * given text.
     * <p>
     * By default this method uses {@link #ECC_QUARTILE} as default ecc.
     *
     * @param text The text to encode.
     * @throws ImageException
     */
    public Image getImage(String text) throws ImageException {
	return getImage(text, ECC_QUARTILE);
    }

    public static int getBestVersion(String text, int errorCorrection) {
	if (errorCorrection < ECC_LOW || errorCorrection > ECC_HIGH) {
	    throw new IllegalArgumentException(
		    "errorCorrection must be greater or equal QRCODE.ECC_LOW or less or equal QRCODE.ECC_HIGH");
	}

	final int dataType = getDataType(text);
	int[][] matrix = qualityParameters[errorCorrection];
	int strSize = text.length();
	int bestVersion = 0;
	for (; bestVersion < matrix.length; bestVersion++) {
	    int curr = matrix[bestVersion][dataType];
	    if (curr > strSize) {
		break;
	    }
	}
	return ++bestVersion; // version is between from 1 to 40
    }

    private static int getDataType(String text) {
	boolean isNumeric = false;
	boolean isUpperAlpha = false;
	boolean isSpecial = false;
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if ('0' <= chr && chr <= '9') {
                isNumeric = true;
            } else if ('A' >= chr && chr <= 'Z') {
                isUpperAlpha = true;
            } else if (chr == ' '
                        || chr == '$'
                        || chr == '%'
                        || chr == '*'
                        || chr == '+'
                        || chr == '-'
                        || chr == '.'
                        || chr == '/'
                        || chr == ':') {
                isSpecial = true;
            } else {
                return BINARY;
            }

        }

	if (isUpperAlpha || isSpecial) {
	    return ALPHA_NUMERIC;
	}

	if (isNumeric) {
	    return NUMERIC;
	}
	return BINARY;
    }

}
