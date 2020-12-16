package totalcross.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;

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
	
	public static I18N aa;
	public static I18N ab;
	public static I18N ae;
	public static I18N af;
	public static I18N ak;
	public static I18N am;
	public static I18N an;
	public static I18N ar;
	public static I18N as;
	public static I18N av;
	public static I18N ay;
	public static I18N az;
	public static I18N ba;
	public static I18N be;
	public static I18N bg;
	public static I18N bh;
	public static I18N bi;
	public static I18N bm;
	public static I18N bn;
	public static I18N bo;
	public static I18N br;
	public static I18N bs;
	public static I18N ca;
	public static I18N ce;
	public static I18N ch;
	public static I18N co;
	public static I18N cr;
	public static I18N cs;
	public static I18N cu;
	public static I18N cv;
	public static I18N cy;
	public static I18N da;
	public static I18N de;
	public static I18N dv;
	public static I18N dz;
	public static I18N ee;
	public static I18N el;
	public static I18N en;
	public static I18N eo;
	public static I18N es;
	public static I18N et;
	public static I18N eu;
	public static I18N fa;
	public static I18N ff;
	public static I18N fi;
	public static I18N fj;
	public static I18N fo;
	public static I18N fr;
	public static I18N fy;
	public static I18N ga;
	public static I18N gd;
	public static I18N gl;
	public static I18N gn;
	public static I18N gu;
	public static I18N gv;
	public static I18N ha;
	public static I18N he;
	public static I18N hi;
	public static I18N ho;
	public static I18N hr;
	public static I18N ht;
	public static I18N hu;
	public static I18N hy;
	public static I18N hz;
	public static I18N ia;
	public static I18N id;
	public static I18N ie;
	public static I18N ig;
	public static I18N ii;
	public static I18N ik;
	public static I18N in;
	public static I18N io;
	public static I18N is;
	public static I18N it;
	public static I18N iu;
	public static I18N iw;
	public static I18N ja;
	public static I18N ji;
	public static I18N jv;
	public static I18N ka;
	public static I18N kg;
	public static I18N ki;
	public static I18N kj;
	public static I18N kk;
	public static I18N kl;
	public static I18N km;
	public static I18N kn;
	public static I18N ko;
	public static I18N kr;
	public static I18N ks;
	public static I18N ku;
	public static I18N kv;
	public static I18N kw;
	public static I18N ky;
	public static I18N la;
	public static I18N lb;
	public static I18N lg;
	public static I18N li;
	public static I18N ln;
	public static I18N lo;
	public static I18N lt;
	public static I18N lu;
	public static I18N lv;
	public static I18N mg;
	public static I18N mh;
	public static I18N mi;
	public static I18N mk;
	public static I18N ml;
	public static I18N mn;
	public static I18N mo;
	public static I18N mr;
	public static I18N ms;
	public static I18N mt;
	public static I18N my;
	public static I18N na;
	public static I18N nb;
	public static I18N nd;
	public static I18N ne;
	public static I18N ng;
	public static I18N nl;
	public static I18N nn;
	public static I18N no;
	public static I18N nr;
	public static I18N nv;
	public static I18N ny;
	public static I18N oc;
	public static I18N oj;
	public static I18N om;
	public static I18N or;
	public static I18N os;
	public static I18N pa;
	public static I18N pi;
	public static I18N pl;
	public static I18N ps;
	public static I18N pt;
	public static I18N qu;
	public static I18N rm;
	public static I18N rn;
	public static I18N ro;
	public static I18N ru;
	public static I18N rw;
	public static I18N sa;
	public static I18N sc;
	public static I18N sd;
	public static I18N se;
	public static I18N sg;
	public static I18N si;
	public static I18N sk;
	public static I18N sl;
	public static I18N sm;
	public static I18N sn;
	public static I18N so;
	public static I18N sq;
	public static I18N sr;
	public static I18N ss;
	public static I18N st;
	public static I18N su;
	public static I18N sv;
	public static I18N sw;
	public static I18N ta;
	public static I18N te;
	public static I18N tg;
	public static I18N th;
	public static I18N ti;
	public static I18N tk;
	public static I18N tl;
	public static I18N tn;
	public static I18N to;
	public static I18N tr;
	public static I18N ts;
	public static I18N tt;
	public static I18N tw;
	public static I18N ty;
	public static I18N ug;
	public static I18N uk;
	public static I18N ur;
	public static I18N uz;
	public static I18N ve;
	public static I18N vi;
	public static I18N vo;
	public static I18N wa;
	public static I18N wo;
	public static I18N xh;
	public static I18N yi;
	public static I18N yo;
	public static I18N za;
	public static I18N zh;
	public static I18N zu;
	public static I18N none;


	private boolean empty = false;
	private HashMap<String, String> map;
	private Locale locale;
	private static boolean alreadyInitted = false;

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
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			//restore default Char Convert
			Convert.charConverter = acc;
		}
	}
	
	/**
	 * Loads all I18N instances
	 * @return
	 */
	public static void initialize() {
		if(alreadyInitted) {
			return;
		}
		
		alreadyInitted = true;
		
		aa = new I18N("aa");
		ab = new I18N("ab");
		ae = new I18N("ae");
		af = new I18N("af");
		ak = new I18N("ak");
		am = new I18N("am");
		an = new I18N("an");
		ar = new I18N("ar");
		as = new I18N("as");
		av = new I18N("av");
		ay = new I18N("ay");
		az = new I18N("az");
		ba = new I18N("ba");
		be = new I18N("be");
		bg = new I18N("bg");
		bh = new I18N("bh");
		bi = new I18N("bi");
		bm = new I18N("bm");
		bn = new I18N("bn");
		bo = new I18N("bo");
		br = new I18N("br");
		bs = new I18N("bs");
		ca = new I18N("ca");
		ce = new I18N("ce");
		ch = new I18N("ch");
		co = new I18N("co");
		cr = new I18N("cr");
		cs = new I18N("cs");
		cu = new I18N("cu");
		cv = new I18N("cv");
		cy = new I18N("cy");
		da = new I18N("da");
		de = new I18N("de");
		dv = new I18N("dv");
		dz = new I18N("dz");
		ee = new I18N("ee");
		el = new I18N("el");
		en = new I18N("en");
		eo = new I18N("eo");
		es = new I18N("es");
		et = new I18N("et");
		eu = new I18N("eu");
		fa = new I18N("fa");
		ff = new I18N("ff");
		fi = new I18N("fi");
		fj = new I18N("fj");
		fo = new I18N("fo");
		fr = new I18N("fr");
		fy = new I18N("fy");
		ga = new I18N("ga");
		gd = new I18N("gd");
		gl = new I18N("gl");
		gn = new I18N("gn");
		gu = new I18N("gu");
		gv = new I18N("gv");
		ha = new I18N("ha");
		he = new I18N("he");
		hi = new I18N("hi");
		ho = new I18N("ho");
		hr = new I18N("hr");
		ht = new I18N("ht");
		hu = new I18N("hu");
		hy = new I18N("hy");
		hz = new I18N("hz");
		ia = new I18N("ia");
		id = new I18N("id");
		ie = new I18N("ie");
		ig = new I18N("ig");
		ii = new I18N("ii");
		ik = new I18N("ik");
		in = new I18N("in");
		io = new I18N("io");
		is = new I18N("is");
		it = new I18N("it");
		iu = new I18N("iu");
		iw = new I18N("iw");
		ja = new I18N("ja");
		ji = new I18N("ji");
		jv = new I18N("jv");
		ka = new I18N("ka");
		kg = new I18N("kg");
		ki = new I18N("ki");
		kj = new I18N("kj");
		kk = new I18N("kk");
		kl = new I18N("kl");
		km = new I18N("km");
		kn = new I18N("kn");
		ko = new I18N("ko");
		kr = new I18N("kr");
		ks = new I18N("ks");
		ku = new I18N("ku");
		kv = new I18N("kv");
		kw = new I18N("kw");
		ky = new I18N("ky");
		la = new I18N("la");
		lb = new I18N("lb");
		lg = new I18N("lg");
		li = new I18N("li");
		ln = new I18N("ln");
		lo = new I18N("lo");
		lt = new I18N("lt");
		lu = new I18N("lu");
		lv = new I18N("lv");
		mg = new I18N("mg");
		mh = new I18N("mh");
		mi = new I18N("mi");
		mk = new I18N("mk");
		ml = new I18N("ml");
		mn = new I18N("mn");
		mo = new I18N("mo");
		mr = new I18N("mr");
		ms = new I18N("ms");
		mt = new I18N("mt");
		my = new I18N("my");
		na = new I18N("na");
		nb = new I18N("nb");
		nd = new I18N("nd");
		ne = new I18N("ne");
		ng = new I18N("ng");
		nl = new I18N("nl");
		nn = new I18N("nn");
		no = new I18N("no");
		nr = new I18N("nr");
		nv = new I18N("nv");
		ny = new I18N("ny");
		oc = new I18N("oc");
		oj = new I18N("oj");
		om = new I18N("om");
		or = new I18N("or");
		os = new I18N("os");
		pa = new I18N("pa");
		pi = new I18N("pi");
		pl = new I18N("pl");
		ps = new I18N("ps");
		pt = new I18N("pt");
		qu = new I18N("qu");
		rm = new I18N("rm");
		rn = new I18N("rn");
		ro = new I18N("ro");
		ru = new I18N("ru");
		rw = new I18N("rw");
		sa = new I18N("sa");
		sc = new I18N("sc");
		sd = new I18N("sd");
		se = new I18N("se");
		sg = new I18N("sg");
		si = new I18N("si");
		sk = new I18N("sk");
		sl = new I18N("sl");
		sm = new I18N("sm");
		sn = new I18N("sn");
		so = new I18N("so");
		sq = new I18N("sq");
		sr = new I18N("sr");
		ss = new I18N("ss");
		st = new I18N("st");
		su = new I18N("su");
		sv = new I18N("sv");
		sw = new I18N("sw");
		ta = new I18N("ta");
		te = new I18N("te");
		tg = new I18N("tg");
		th = new I18N("th");
		ti = new I18N("ti");
		tk = new I18N("tk");
		tl = new I18N("tl");
		tn = new I18N("tn");
		to = new I18N("to");
		tr = new I18N("tr");
		ts = new I18N("ts");
		tt = new I18N("tt");
		tw = new I18N("tw");
		ty = new I18N("ty");
		ug = new I18N("ug");
		uk = new I18N("uk");
		ur = new I18N("ur");
		uz = new I18N("uz");
		ve = new I18N("ve");
		vi = new I18N("vi");
		vo = new I18N("vo");
		wa = new I18N("wa");
		wo = new I18N("wo");
		xh = new I18N("xh");
		yi = new I18N("yi");
		yo = new I18N("yo");
		za = new I18N("za");
		zh = new I18N("zh");
		zu = new I18N("zu");
		none = new I18N("default");
		
		
	}

	/**
	 * Returns an instance of I18N for the specified ISO 639 code.
	 * @param iso639Code
	 * @return
	 */
	public static I18N getI18N (String iso639Code) {
		
		if(iso639Code.equals("aa")) return aa;
		if(iso639Code.equals("ab")) return ab;
		if(iso639Code.equals("ae")) return ae;
		if(iso639Code.equals("af")) return af;
		if(iso639Code.equals("ak")) return ak;
		if(iso639Code.equals("am")) return am;
		if(iso639Code.equals("an")) return an;
		if(iso639Code.equals("ar")) return ar;
		if(iso639Code.equals("as")) return as;
		if(iso639Code.equals("av")) return av;
		if(iso639Code.equals("ay")) return ay;
		if(iso639Code.equals("az")) return az;
		if(iso639Code.equals("ba")) return ba;
		if(iso639Code.equals("be")) return be;
		if(iso639Code.equals("bg")) return bg;
		if(iso639Code.equals("bh")) return bh;
		if(iso639Code.equals("bi")) return bi;
		if(iso639Code.equals("bm")) return bm;
		if(iso639Code.equals("bn")) return bn;
		if(iso639Code.equals("bo")) return bo;
		if(iso639Code.equals("br")) return br;
		if(iso639Code.equals("bs")) return bs;
		if(iso639Code.equals("ca")) return ca;
		if(iso639Code.equals("ce")) return ce;
		if(iso639Code.equals("ch")) return ch;
		if(iso639Code.equals("co")) return co;
		if(iso639Code.equals("cr")) return cr;
		if(iso639Code.equals("cs")) return cs;
		if(iso639Code.equals("cu")) return cu;
		if(iso639Code.equals("cv")) return cv;
		if(iso639Code.equals("cy")) return cy;
		if(iso639Code.equals("da")) return da;
		if(iso639Code.equals("de")) return de;
		if(iso639Code.equals("dv")) return dv;
		if(iso639Code.equals("dz")) return dz;
		if(iso639Code.equals("ee")) return ee;
		if(iso639Code.equals("el")) return el;
		if(iso639Code.equals("en")) return en;
		if(iso639Code.equals("eo")) return eo;
		if(iso639Code.equals("es")) return es;
		if(iso639Code.equals("et")) return et;
		if(iso639Code.equals("eu")) return eu;
		if(iso639Code.equals("fa")) return fa;
		if(iso639Code.equals("ff")) return ff;
		if(iso639Code.equals("fi")) return fi;
		if(iso639Code.equals("fj")) return fj;
		if(iso639Code.equals("fo")) return fo;
		if(iso639Code.equals("fr")) return fr;
		if(iso639Code.equals("fy")) return fy;
		if(iso639Code.equals("ga")) return ga;
		if(iso639Code.equals("gd")) return gd;
		if(iso639Code.equals("gl")) return gl;
		if(iso639Code.equals("gn")) return gn;
		if(iso639Code.equals("gu")) return gu;
		if(iso639Code.equals("gv")) return gv;
		if(iso639Code.equals("ha")) return ha;
		if(iso639Code.equals("he")) return he;
		if(iso639Code.equals("hi")) return hi;
		if(iso639Code.equals("ho")) return ho;
		if(iso639Code.equals("hr")) return hr;
		if(iso639Code.equals("ht")) return ht;
		if(iso639Code.equals("hu")) return hu;
		if(iso639Code.equals("hy")) return hy;
		if(iso639Code.equals("hz")) return hz;
		if(iso639Code.equals("ia")) return ia;
		if(iso639Code.equals("id")) return id;
		if(iso639Code.equals("ie")) return ie;
		if(iso639Code.equals("ig")) return ig;
		if(iso639Code.equals("ii")) return ii;
		if(iso639Code.equals("ik")) return ik;
		if(iso639Code.equals("in")) return in;
		if(iso639Code.equals("io")) return io;
		if(iso639Code.equals("is")) return is;
		if(iso639Code.equals("it")) return it;
		if(iso639Code.equals("iu")) return iu;
		if(iso639Code.equals("iw")) return iw;
		if(iso639Code.equals("ja")) return ja;
		if(iso639Code.equals("ji")) return ji;
		if(iso639Code.equals("jv")) return jv;
		if(iso639Code.equals("ka")) return ka;
		if(iso639Code.equals("kg")) return kg;
		if(iso639Code.equals("ki")) return ki;
		if(iso639Code.equals("kj")) return kj;
		if(iso639Code.equals("kk")) return kk;
		if(iso639Code.equals("kl")) return kl;
		if(iso639Code.equals("km")) return km;
		if(iso639Code.equals("kn")) return kn;
		if(iso639Code.equals("ko")) return ko;
		if(iso639Code.equals("kr")) return kr;
		if(iso639Code.equals("ks")) return ks;
		if(iso639Code.equals("ku")) return ku;
		if(iso639Code.equals("kv")) return kv;
		if(iso639Code.equals("kw")) return kw;
		if(iso639Code.equals("ky")) return ky;
		if(iso639Code.equals("la")) return la;
		if(iso639Code.equals("lb")) return lb;
		if(iso639Code.equals("lg")) return lg;
		if(iso639Code.equals("li")) return li;
		if(iso639Code.equals("ln")) return ln;
		if(iso639Code.equals("lo")) return lo;
		if(iso639Code.equals("lt")) return lt;
		if(iso639Code.equals("lu")) return lu;
		if(iso639Code.equals("lv")) return lv;
		if(iso639Code.equals("mg")) return mg;
		if(iso639Code.equals("mh")) return mh;
		if(iso639Code.equals("mi")) return mi;
		if(iso639Code.equals("mk")) return mk;
		if(iso639Code.equals("ml")) return ml;
		if(iso639Code.equals("mn")) return mn;
		if(iso639Code.equals("mo")) return mo;
		if(iso639Code.equals("mr")) return mr;
		if(iso639Code.equals("ms")) return ms;
		if(iso639Code.equals("mt")) return mt;
		if(iso639Code.equals("my")) return my;
		if(iso639Code.equals("na")) return na;
		if(iso639Code.equals("nb")) return nb;
		if(iso639Code.equals("nd")) return nd;
		if(iso639Code.equals("ne")) return ne;
		if(iso639Code.equals("ng")) return ng;
		if(iso639Code.equals("nl")) return nl;
		if(iso639Code.equals("nn")) return nn;
		if(iso639Code.equals("no")) return no;
		if(iso639Code.equals("nr")) return nr;
		if(iso639Code.equals("nv")) return nv;
		if(iso639Code.equals("ny")) return ny;
		if(iso639Code.equals("oc")) return oc;
		if(iso639Code.equals("oj")) return oj;
		if(iso639Code.equals("om")) return om;
		if(iso639Code.equals("or")) return or;
		if(iso639Code.equals("os")) return os;
		if(iso639Code.equals("pa")) return pa;
		if(iso639Code.equals("pi")) return pi;
		if(iso639Code.equals("pl")) return pl;
		if(iso639Code.equals("ps")) return ps;
		if(iso639Code.equals("pt")) return pt;
		if(iso639Code.equals("qu")) return qu;
		if(iso639Code.equals("rm")) return rm;
		if(iso639Code.equals("rn")) return rn;
		if(iso639Code.equals("ro")) return ro;
		if(iso639Code.equals("ru")) return ru;
		if(iso639Code.equals("rw")) return rw;
		if(iso639Code.equals("sa")) return sa;
		if(iso639Code.equals("sc")) return sc;
		if(iso639Code.equals("sd")) return sd;
		if(iso639Code.equals("se")) return se;
		if(iso639Code.equals("sg")) return sg;
		if(iso639Code.equals("si")) return si;
		if(iso639Code.equals("sk")) return sk;
		if(iso639Code.equals("sl")) return sl;
		if(iso639Code.equals("sm")) return sm;
		if(iso639Code.equals("sn")) return sn;
		if(iso639Code.equals("so")) return so;
		if(iso639Code.equals("sq")) return sq;
		if(iso639Code.equals("sr")) return sr;
		if(iso639Code.equals("ss")) return ss;
		if(iso639Code.equals("st")) return st;
		if(iso639Code.equals("su")) return su;
		if(iso639Code.equals("sv")) return sv;
		if(iso639Code.equals("sw")) return sw;
		if(iso639Code.equals("ta")) return ta;
		if(iso639Code.equals("te")) return te;
		if(iso639Code.equals("tg")) return tg;
		if(iso639Code.equals("th")) return th;
		if(iso639Code.equals("ti")) return ti;
		if(iso639Code.equals("tk")) return tk;
		if(iso639Code.equals("tl")) return tl;
		if(iso639Code.equals("tn")) return tn;
		if(iso639Code.equals("to")) return to;
		if(iso639Code.equals("tr")) return tr;
		if(iso639Code.equals("ts")) return ts;
		if(iso639Code.equals("tt")) return tt;
		if(iso639Code.equals("tw")) return tw;
		if(iso639Code.equals("ty")) return ty;
		if(iso639Code.equals("ug")) return ug;
		if(iso639Code.equals("uk")) return uk;
		if(iso639Code.equals("ur")) return ur;
		if(iso639Code.equals("uz")) return uz;
		if(iso639Code.equals("ve")) return ve;
		if(iso639Code.equals("vi")) return vi;
		if(iso639Code.equals("vo")) return vo;
		if(iso639Code.equals("wa")) return wa;
		if(iso639Code.equals("wo")) return wo;
		if(iso639Code.equals("xh")) return xh;
		if(iso639Code.equals("yi")) return yi;
		if(iso639Code.equals("yo")) return yo;
		if(iso639Code.equals("za")) return za;
		if(iso639Code.equals("zh")) return zh;
		if(iso639Code.equals("zu")) return zu;

		return none;
		
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
