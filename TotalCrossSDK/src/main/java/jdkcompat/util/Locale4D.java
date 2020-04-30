// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.util.LangTag;

/**
 * A Locale object represents a specific geographical, political, or cultural region.
 * An operation that requires a Locale to perform its task is called locale-sensitive
 * and uses the Locale to tailor information for the user. For example, displaying a
 * number is a locale-sensitive operation— the number should be formatted according
 * to the customs and conventions of the user's native country, region, or culture.
 */
public class Locale4D {
	
	private String language;
	private String iSO3language;
	private String country;
	private String iSO3country;
	private String variant;
	private String script;
	private String extension;
	
	private static String [][] iSOToISO3Languages = 
			{
					{"aa", "aar"}, {"ab", "abk"}, {"ae", "ave"}, {"af", "afr"}, {"ak", "aka"}, 
					{"am", "amh"}, {"an", "arg"}, {"ar", "ara"}, {"as", "asm"}, {"av", "ava"}, 
					{"ay", "aym"}, {"az", "aze"}, {"ba", "bak"}, {"be", "bel"}, {"bg", "bul"}, 
					{"bh", "bih"}, {"bi", "bis"}, {"bm", "bam"}, {"bn", "ben"}, {"bo", "bod"}, 
					{"br", "bre"}, {"bs", "bos"}, {"ca", "cat"}, {"ce", "che"}, {"ch", "cha"}, 
					{"co", "cos"}, {"cr", "cre"}, {"cs", "ces"}, {"cu", "chu"}, {"cv", "chv"}, 
					{"cy", "cym"}, {"da", "dan"}, {"de", "deu"}, {"dv", "div"}, {"dz", "dzo"}, 
					{"ee", "ewe"}, {"el", "ell"}, {"en", "eng"}, {"eo", "epo"}, {"es", "spa"}, 
					{"et", "est"}, {"eu", "eus"}, {"fa", "fas"}, {"ff", "ful"}, {"fi", "fin"}, 
					{"fj", "fij"}, {"fo", "fao"}, {"fr", "fra"}, {"fy", "fry"}, {"ga", "gle"}, 
					{"gd", "gla"}, {"gl", "glg"}, {"gn", "grn"}, {"gu", "guj"}, {"gv", "glv"}, 
					{"ha", "hau"}, {"he", "heb"}, {"hi", "hin"}, {"ho", "hmo"}, {"hr", "hrv"}, 
					{"ht", "hat"}, {"hu", "hun"}, {"hy", "hye"}, {"hz", "her"}, {"ia", "ina"}, 
					{"id", "ind"}, {"ie", "ile"}, {"ig", "ibo"}, {"ii", "iii"}, {"ik", "ipk"}, 
					{"in", "ind"}, {"io", "ido"}, {"is", "isl"}, {"it", "ita"}, {"iu", "iku"}, 
					{"iw", "heb"}, {"ja", "jpn"}, {"ji", "yid"}, {"jv", "jav"}, {"ka", "kat"}, 
					{"kg", "kon"}, {"ki", "kik"}, {"kj", "kua"}, {"kk", "kaz"}, {"kl", "kal"}, 
					{"km", "khm"}, {"kn", "kan"}, {"ko", "kor"}, {"kr", "kau"}, {"ks", "kas"}, 
					{"ku", "kur"}, {"kv", "kom"}, {"kw", "cor"}, {"ky", "kir"}, {"la", "lat"}, 
					{"lb", "ltz"}, {"lg", "lug"}, {"li", "lim"}, {"ln", "lin"}, {"lo", "lao"}, 
					{"lt", "lit"}, {"lu", "lub"}, {"lv", "lav"}, {"mg", "mlg"}, {"mh", "mah"}, 
					{"mi", "mri"}, {"mk", "mkd"}, {"ml", "mal"}, {"mn", "mon"}, {"mo", "mol"}, 
					{"mr", "mar"}, {"ms", "msa"}, {"mt", "mlt"}, {"my", "mya"}, {"na", "nau"}, 
					{"nb", "nob"}, {"nd", "nde"}, {"ne", "nep"}, {"ng", "ndo"}, {"nl", "nld"}, 
					{"nn", "nno"}, {"no", "nor"}, {"nr", "nbl"}, {"nv", "nav"}, {"ny", "nya"}, 
					{"oc", "oci"}, {"oj", "oji"}, {"om", "orm"}, {"or", "ori"}, {"os", "oss"}, 
					{"pa", "pan"}, {"pi", "pli"}, {"pl", "pol"}, {"ps", "pus"}, {"pt", "por"}, 
					{"qu", "que"}, {"rm", "roh"}, {"rn", "run"}, {"ro", "ron"}, {"ru", "rus"}, 
					{"rw", "kin"}, {"sa", "san"}, {"sc", "srd"}, {"sd", "snd"}, {"se", "sme"}, 
					{"sg", "sag"}, {"si", "sin"}, {"sk", "slk"}, {"sl", "slv"}, {"sm", "smo"}, 
					{"sn", "sna"}, {"so", "som"}, {"sq", "sqi"}, {"sr", "srp"}, {"ss", "ssw"}, 
					{"st", "sot"}, {"su", "sun"}, {"sv", "swe"}, {"sw", "swa"}, {"ta", "tam"}, 
					{"te", "tel"}, {"tg", "tgk"}, {"th", "tha"}, {"ti", "tir"}, {"tk", "tuk"}, 
					{"tl", "tgl"}, {"tn", "tsn"}, {"to", "ton"}, {"tr", "tur"}, {"ts", "tso"}, 
					{"tt", "tat"}, {"tw", "twi"}, {"ty", "tah"}, {"ug", "uig"}, {"uk", "ukr"}, 
					{"ur", "urd"}, {"uz", "uzb"}, {"ve", "ven"}, {"vi", "vie"}, {"vo", "vol"}, 
					{"wa", "wln"}, {"wo", "wol"}, {"xh", "xho"}, {"yi", "yid"}, {"yo", "yor"}, 
					{"za", "zha"}, {"zh", "zho"}, {"zu", "zul"} 
			};
	private static String [][] iSOToISO3Countries = 
			{
					{"AD", "AND"}, {"AE", "ARE"}, {"AF", "AFG"}, {"AG", "ATG"}, {"AI", "AIA"}, 
					{"AL", "ALB"}, {"AM", "ARM"}, {"AN", "ANT"}, {"AO", "AGO"}, {"AQ", "ATA"}, 
					{"AR", "ARG"}, {"AS", "ASM"}, {"AT", "AUT"}, {"AU", "AUS"}, {"AW", "ABW"}, 
					{"AX", "ALA"}, {"AZ", "AZE"}, {"BA", "BIH"}, {"BB", "BRB"}, {"BD", "BGD"}, 
					{"BE", "BEL"}, {"BF", "BFA"}, {"BG", "BGR"}, {"BH", "BHR"}, {"BI", "BDI"}, 
					{"BJ", "BEN"}, {"BL", "BLM"}, {"BM", "BMU"}, {"BN", "BRN"}, {"BO", "BOL"}, 
					{"BQ", "BES"}, {"BR", "BRA"}, {"BS", "BHS"}, {"BT", "BTN"}, {"BV", "BVT"}, 
					{"BW", "BWA"}, {"BY", "BLR"}, {"BZ", "BLZ"}, {"CA", "CAN"}, {"CC", "CCK"}, 
					{"CD", "COD"}, {"CF", "CAF"}, {"CG", "COG"}, {"CH", "CHE"}, {"CI", "CIV"}, 
					{"CK", "COK"}, {"CL", "CHL"}, {"CM", "CMR"}, {"CN", "CHN"}, {"CO", "COL"}, 
					{"CR", "CRI"}, {"CU", "CUB"}, {"CV", "CPV"}, {"CW", "CUW"}, {"CX", "CXR"}, 
					{"CY", "CYP"}, {"CZ", "CZE"}, {"DE", "DEU"}, {"DJ", "DJI"}, {"DK", "DNK"}, 
					{"DM", "DMA"}, {"DO", "DOM"}, {"DZ", "DZA"}, {"EC", "ECU"}, {"EE", "EST"}, 
					{"EG", "EGY"}, {"EH", "ESH"}, {"ER", "ERI"}, {"ES", "ESP"}, {"ET", "ETH"}, 
					{"FI", "FIN"}, {"FJ", "FJI"}, {"FK", "FLK"}, {"FM", "FSM"}, {"FO", "FRO"}, 
					{"FR", "FRA"}, {"GA", "GAB"}, {"GB", "GBR"}, {"GD", "GRD"}, {"GE", "GEO"}, 
					{"GF", "GUF"}, {"GG", "GGY"}, {"GH", "GHA"}, {"GI", "GIB"}, {"GL", "GRL"}, 
					{"GM", "GMB"}, {"GN", "GIN"}, {"GP", "GLP"}, {"GQ", "GNQ"}, {"GR", "GRC"}, 
					{"GS", "SGS"}, {"GT", "GTM"}, {"GU", "GUM"}, {"GW", "GNB"}, {"GY", "GUY"}, 
					{"HK", "HKG"}, {"HM", "HMD"}, {"HN", "HND"}, {"HR", "HRV"}, {"HT", "HTI"}, 
					{"HU", "HUN"}, {"ID", "IDN"}, {"IE", "IRL"}, {"IL", "ISR"}, {"IM", "IMN"}, 
					{"IN", "IND"}, {"IO", "IOT"}, {"IQ", "IRQ"}, {"IR", "IRN"}, {"IS", "ISL"}, 
					{"IT", "ITA"}, {"JE", "JEY"}, {"JM", "JAM"}, {"JO", "JOR"}, {"JP", "JPN"}, 
					{"KE", "KEN"}, {"KG", "KGZ"}, {"KH", "KHM"}, {"KI", "KIR"}, {"KM", "COM"}, 
					{"KN", "KNA"}, {"KP", "PRK"}, {"KR", "KOR"}, {"KW", "KWT"}, {"KY", "CYM"}, 
					{"KZ", "KAZ"}, {"LA", "LAO"}, {"LB", "LBN"}, {"LC", "LCA"}, {"LI", "LIE"}, 
					{"LK", "LKA"}, {"LR", "LBR"}, {"LS", "LSO"}, {"LT", "LTU"}, {"LU", "LUX"}, 
					{"LV", "LVA"}, {"LY", "LBY"}, {"MA", "MAR"}, {"MC", "MCO"}, {"MD", "MDA"}, 
					{"ME", "MNE"}, {"MF", "MAF"}, {"MG", "MDG"}, {"MH", "MHL"}, {"MK", "MKD"}, 
					{"ML", "MLI"}, {"MM", "MMR"}, {"MN", "MNG"}, {"MO", "MAC"}, {"MP", "MNP"}, 
					{"MQ", "MTQ"}, {"MR", "MRT"}, {"MS", "MSR"}, {"MT", "MLT"}, {"MU", "MUS"}, 
					{"MV", "MDV"}, {"MW", "MWI"}, {"MX", "MEX"}, {"MY", "MYS"}, {"MZ", "MOZ"}, 
					{"NA", "NAM"}, {"NC", "NCL"}, {"NE", "NER"}, {"NF", "NFK"}, {"NG", "NGA"}, 
					{"NI", "NIC"}, {"NL", "NLD"}, {"NO", "NOR"}, {"NP", "NPL"}, {"NR", "NRU"}, 
					{"NU", "NIU"}, {"NZ", "NZL"}, {"OM", "OMN"}, {"PA", "PAN"}, {"PE", "PER"}, 
					{"PF", "PYF"}, {"PG", "PNG"}, {"PH", "PHL"}, {"PK", "PAK"}, {"PL", "POL"}, 
					{"PM", "SPM"}, {"PN", "PCN"}, {"PR", "PRI"}, {"PS", "PSE"}, {"PT", "PRT"}, 
					{"PW", "PLW"}, {"PY", "PRY"}, {"QA", "QAT"}, {"RE", "REU"}, {"RO", "ROU"}, 
					{"RS", "SRB"}, {"RU", "RUS"}, {"RW", "RWA"}, {"SA", "SAU"}, {"SB", "SLB"}, 
					{"SC", "SYC"}, {"SD", "SDN"}, {"SE", "SWE"}, {"SG", "SGP"}, {"SH", "SHN"}, 
					{"SI", "SVN"}, {"SJ", "SJM"}, {"SK", "SVK"}, {"SL", "SLE"}, {"SM", "SMR"}, 
					{"SN", "SEN"}, {"SO", "SOM"}, {"SR", "SUR"}, {"SS", "SSD"}, {"ST", "STP"}, 
					{"SV", "SLV"}, {"SX", "SXM"}, {"SY", "SYR"}, {"SZ", "SWZ"}, {"TC", "TCA"}, 
					{"TD", "TCD"}, {"TF", "ATF"}, {"TG", "TGO"}, {"TH", "THA"}, {"TJ", "TJK"}, 
					{"TK", "TKL"}, {"TL", "TLS"}, {"TM", "TKM"}, {"TN", "TUN"}, {"TO", "TON"}, 
					{"TR", "TUR"}, {"TT", "TTO"}, {"TV", "TUV"}, {"TW", "TWN"}, {"TZ", "TZA"}, 
					{"UA", "UKR"}, {"UG", "UGA"}, {"UM", "UMI"}, {"US", "USA"}, {"UY", "URY"}, 
					{"UZ", "UZB"}, {"VA", "VAT"}, {"VC", "VCT"}, {"VE", "VEN"}, {"VG", "VGB"}, 
					{"VI", "VIR"}, {"VN", "VNM"}, {"VU", "VUT"}, {"WF", "WLF"}, {"WS", "WSM"}, 
					{"YE", "YEM"}, {"YT", "MYT"}, {"ZA", "ZAF"}, {"ZM", "ZMB"}, {"ZW", "ZWE"}, 
			}; 
	

	
	private static final String [] languages = 
		{
				"aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av",
				"ay", "az", "ba", "be", "bg",	"bh", "bi", "bm", "bn", "bo", 
				"br", "bs", "ca", "ce", "ch",	"co", "cr", "cs", "cu", "cv", 
				"cy", "da", "de", "dv", "dz",	"ee", "el", "en", "eo", "es", 
				"et", "eu", "fa", "ff", "fi",	"fj", "fo", "fr", "fy", "ga", 
				"gd", "gl", "gn", "gu", "gv",	"ha", "he", "hi", "ho", "hr", 
				"ht", "hu", "hy", "hz", "ia",	"id", "ie", "ig", "ii", "ik", 
				"in", "io", "is", "it", "iu",	"iw", "ja", "ji", "jv", "ka", 
				"kg", "ki", "kj", "kk", "kl",	"km", "kn", "ko", "kr", "ks", 
				"ku", "kv", "kw", "ky", "la",	"lb", "lg", "li", "ln", "lo", 
				"lt", "lu", "lv", "mg", "mh",	"mi", "mk", "ml", "mn", "mo", 
				"mr", "ms", "mt", "my", "na",	"nb", "nd", "ne", "ng", "nl", 
				"nn", "no", "nr", "nv", "ny",	"oc", "oj", "om", "or", "os", 
				"pa", "pi", "pl", "ps", "pt",	"qu", "rm", "rn", "ro", "ru", 
				"rw", "sa", "sc", "sd", "se", "sg", "si", "sk", "sl", "sm", 
				"sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", 
				"te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", 
				"tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", 
				"wa", "wo", "xh", "yi", "yo", "za", "zh", "zu"
		};
	
	private static final String [] countries =
		{
				"AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", 
				"AR", "AS", "AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BD", 
				"BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", 
				"BQ", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", 
				"CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", 
				"CR", "CU", "CV", "CW", "CX", "CY", "CZ", "DE", "DJ", "DK", 
				"DM", "DO", "DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET", 
				"FI", "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE", 
				"GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", 
				"GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", 
				"HU", "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", 
				"IT", "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", 
				"KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", 
				"LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", 
				"ME", "MF", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", 
				"MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", 
				"NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", 
				"NU", "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", 
				"PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", 
				"RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", 
				"SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", 
				"SV", "SX", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", 
				"TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ", 
				"UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", 
				"VI", "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW", 
		};

	/**
	 * Constructs a locale from language.
	 * @param language
	 */
	Locale4D(String language) {
		this.language = language;
	}

	/**
	 * Constructs a locale from language and country.
	 * @param language
	 * @param country
	 */
	Locale4D(String language, String country) {
		this.language = language;
		this.country = country;
	}

	/**
	 * Constructs a locale from langauge and country and variant.
	 * @param language
	 * @param country
	 * @param variant
	 */
	Locale4D(String language, String country, String variant) {
		this.language = language;
		this.country = country;
		this.variant = variant;
	}

	/**
	 * Returns a locale for the specified IETF BCP 47 language tag string.
	 * If the specified language tag contains any ill-formed subtags, the first such subtag and
	 * all following subtags are ignored. Compare to Locale.Builder.setLanguageTag(java.lang.String)
	 * which throws an exception in this case.
	 * @param languageTag
	 * @return
	 */
	public static Locale4D forLanguageTag(String languageTag) {
		String [] atts = LangTag.parse(languageTag);	
		return new Locale4D.Builder()
				.setLanguage(atts[LangTag.idxLanguage])
				.setRegion(atts[LangTag.idxRegion])
				.setScript(atts[LangTag.idxScript])
				.setVariant(atts[LangTag.idxVariant])
				.setExtension(atts[LangTag.idxExtension])
				.build();
	}

	/**
	 * Returns a list of all 2-letter language codes defined in ISO 639. Can be used to create Locales.
	 * @return array of ISO 639 languages
	 */
	public static String [] getISOLanguages() { return languages;}


	/**
	 * Get User Default Locale
	 * @return an Locale object containing default information of the system.
	 * @apiNote: Not working on linux based systems. The value on these systems will be null.
	 *
	 */
	public static Locale4D getDefault() {
		String locale = getDefaultToString().replace("_", "-");
		if(locale != null) return forLanguageTag(locale);
		return null;
	}

	/**
	 * Returns locale in the String format: language (ISO 639)-country (ISO 3166 2-letter code).
	 * Example: pt-BR, en-US.
	 * @return an String in the format: language (ISO 639)-country (ISO 3166 2-letter code).
	 */
	@ReplacedByNativeOnDeploy
	static String getDefaultToString() {
		return java.util.Locale.getDefault().toString();
	}

	/**
	 * Returns the language code of this Locale according to ISO 639.
	 * @return The language code, or the empty string if none is defined.
	 */
	public String getLanguage() {
		return language;
	}

	//TODO
	/*public String getDisplayLanguage() {
		return language;
	}*/

	/**
	 * Returns the country/region code for this locale, which should either be the empty string, an uppercase ISO 3166
	 * 2-letter code.
	 * @return The country/region code, or the empty string if none is defined.
	 */
	public String getCountry() {
		return country;
	}

	// TODO
	/*public String getDisplayCountry() {
		return country;
	}*/

	/**
	 * Returns the set of extension keys associated with this locale.
	 * @return The extension, or null if this locale defines no extension for this Locale
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Returns a three-letter abbreviation for this locale's country. If the country matches an ISO 3166-1 alpha-2 code, the corresponding ISO 3166-1 alpha-3 uppercase code is returned. If the locale doesn't specify a country, this will be the empty string.
	 * The ISO 3166-1 codes can be found on-line.
	 * @return A three-letter abbreviation of this locale's language.
	 */
	public String getISO3Country() {
		return iSO3country;
	}

	/**
	 * Returns a three-letter abbreviation of this locale's language. If the language matches an ISO 639-1
	 * two-letter code, the corresponding ISO 639-2/T three-letter lowercase code is returned. The ISO 639-2
	 * language codes can be found on-line, see "Codes for the Representation of Names of Languages Part 2:
	 * Alpha-3 Code". If the locale specifies a three-letter language, the language is returned as is.
	 * If the locale does not specify a language the empty string is returned.
	 * Returns:
	 * @return A three-letter abbreviation of this locale's language.
	 */
	public String getISO3Language() {
		return iSO3language;
	}

	/**
	 * Returns the script for this locale, which should either be the empty string or an ISO 15924 4-letter script code.
	 * @return The script code, or the empty string if none is defined.
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Returns the variant code for this locale.
	 * @return The variant code, or the empty string if none is defined.
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * Returns a string representation of this Locale object, consisting of language, country, variant, script, and extensions as below:
	 * language + "_" + country + "_" + (variant + "_#" | "#") + script + "-" + extensions
	 * @return A string representation of the Locale.
	 */
	public String toString() {
		String result = "";
		if(language != null || country != null) {
			result += language != null ? language : "";
			result += country != null ? "_" + country : "";
			result += variant != null ? variant : "";
			if(script != null || extension != null) {
				result += variant != null ? "_#" : "#";
				result += script != null ? script : "";
				result += extension != null ? "-" + extension : "";
			}
		}
		return result;
	}

	/**
	 * Builder is used to build instances of Locale from values configured by the setters.
	 * Unlike the Locale constructors, the Builder checks if a value configured by a setter
	 * satisfies the syntax requirements defined by the Locale class. A Locale object created
	 * by a Builder is well-formed and can be transformed to a well-formed IETF BCP 47 language
	 * tag without losing information.
	 */
	public static class Builder {
		
		
		Locale4D locale = new Locale4D("");
		
		public Builder () {}
		
		public Builder clear () {
			locale = new Locale4D("");
			return this;
		}
		
		public Builder clearExtensions () {
			locale.extension = "";
			return this;
		}
		
		public Builder setExtension (String extension) {
			locale.extension = extension;
			return this;
		}
		
		public Builder setLanguage (String language) {		
			if(language.length() == 3) {
				locale.iSO3language = language;
				locale.language = searchISO(language, iSOToISO3Languages, false);
			}
			if(language.length() == 2) {
				locale.language = language;
				locale.iSO3language = searchISO(language, iSOToISO3Languages, true);
			}
			return this;
		}
		
		public Builder setLanguageTag (String languageTag) {
			locale = forLanguageTag(languageTag);
			return this;
		}
		
		public Builder setLocale(Locale4D locale) {
			this.locale = new Locale4D("");
			this.locale.language = locale.language;
			this.locale.country = locale.country;
			this.locale.extension = locale.extension;
			this.locale.script = locale.script;
			this.locale.variant = locale.variant;
			return this;
		}
		
		public Builder setRegion (String region) {
			if(region.length() == 3) {
				locale.iSO3country = region;
				locale.country = searchISO(region, iSOToISO3Countries, false);
			}
			if(region.length() == 2) {
				locale.country = region;
				locale.iSO3country = searchISO(region, iSOToISO3Countries, true);
			}
			return this;
		}
		
		public Builder setScript (String script) {
			locale.script = script;
			return this;
		}
		
		public Builder setVariant (String variant) {
			locale.variant = variant;
			return this;
		}
				
		public Locale4D build () {return locale;}
		
		private String searchISO(String value, String [][] v, boolean isoTOiso3) {
			if(isoTOiso3) {
				for (int i = 0; i < v.length; i++) {
					if(v[i][0].equals(value)) return v[i][1];
				}
			}
			else {
				for (int i = 0; i < v.length; i++) {
					if(v[i][1].equals(value)) return v[i][0];
				}
			}
					
			return null;
		}
	}
	
}
