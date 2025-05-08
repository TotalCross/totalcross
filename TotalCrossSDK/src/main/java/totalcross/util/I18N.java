package totalcross.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import totalcross.sys.AbstractCharacterConverter;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.xml.AttributeList;
import totalcross.xml.ContentHandler;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlReader;

/**
 * I18N is entity that supports internationalized applications. Its structure contains a Map of all values
 * registred in an xml file. For instance, if you want to define values for Portguese language whose ISO 639 code is
 * pt, then create a xml file with the path <i>&lt;source patch&lt;/resources/i18n/pt.xml</i>. contaning the following format:
 * <pre>
 * &lt;resources&gt;
 * 	&lt;string name="hello_world"&gt;Olá mundo!&lt;/string&gt;
 * &lt;/resources&gt;
 * </pre>
 *
 * Then initialize values on the MainWindow Constructor an get <i>hello_world</i> value as follow:
 * <pre>
 *     public class App extends MainWindow {
 *     public App() {
 *         I18N.initialize();
 *     }
 *
 *     &#64;Override
 *     public void initUI() {
 *     		I18N.pt.get("hello_world");
 *     }
 * </pre>
 *
 *
 *
 */
public class I18N {

	private static final Map<String, I18N> codes = new HashMap<>();
	
	private boolean empty = false;
	private Map<String, String> map;

	public static I18N none;

	static {
		none = new I18N("default");
	}

	/**
	 * Determines if this I18N has values defined
	 * @return
	 */
	private boolean isEmpty() {
		return empty;
	}
	
	private I18N (String iso639Code) {
		
		map = new HashMap<String, String>();
		byte [] file = Vm.getFile("resources/i18n/"+iso639Code+".xml");
		if(file == null) {
			empty = true;
		}
		else {

			AbstractCharacterConverter acc = Convert.charConverter;
			Convert.setDefaultConverter("UTF-8");
			XmlReader rdr = new XmlReader();
			
			rdr.setContentHandler(new ContentHandler() {
				int tag = 0;
				String att_value;
				String value;
				@Override
				public void startElement(int tag, AttributeList atts) {
					String aux = atts.getAttributeValue("name");
					if(aux != null) {
						this.tag = tag;
						att_value = aux;
						
					}
				}
				
				@Override
				public void endElement(int tag) {
					if(this.tag == tag) {
						map.put(att_value, value);
					}
				}
				
				@Override
				public void characters(String s) {
					value = s;
				}
			});
			try {
				rdr.parse(Convert.charConverter.bytes2chars(file, 0, file.length), 0, file.length);
			} catch (SyntaxException e) {
				e.printStackTrace();
			}
			//restore default Char Convert
			Convert.charConverter = acc;
		}
	}
	
	/**
	 * No longer required, maps are loaded on demand.
	 * Will be removed in next major release.
	 */
	@Deprecated
	public static void initialize() {
	}

	/**
	 * Returns an instance of I18N for the specified ISO 639 code.
	 * @param iso639Code
	 * @return
	 */
	public static I18N getI18N (String iso639Code) {
		I18N code = codes.get(iso639Code);
		if (code == null) {
			code = new I18N(iso639Code);
			codes.put(iso639Code, code);
		}
		return code;
	}

	/**
	 * Returns value for a defined key for this I18N instance.
	 * @param name
	 * @return
	 */
	public String getValue(String name) {return map.get(name);}

	/**
	 * Returns I18N for the device default language. If there is no xml file for this language,
	 * this function will return an I18N instance with values defined in <i>
	 * &lt;source path&gt;/resources/i18n/default.xml</i>
	 * @return the default I18N instance
	 */
	public static I18N getDefault() {
		I18N i18N = getI18N(Locale.getDefault().getLanguage());
		if(i18N.isEmpty()) {
			return none;
		} else {
			return i18N;
		}
	}
}
