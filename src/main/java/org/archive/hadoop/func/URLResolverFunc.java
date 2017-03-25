package org.archive.hadoop.func;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

public class URLResolverFunc extends EvalFunc<String> {
	private static final Logger LOG =
		Logger.getLogger(URLResolverFunc.class.getName());

	private URL baseURL;
	private String lastBase;

	public URLResolverFunc() {
		baseURL = null;
		lastBase = null;
	}

	private boolean isAbsolute(String url) {
		return url.startsWith("http://")
			|| url.startsWith("https://")
			|| url.startsWith("ftp://")
			|| url.startsWith("feed://")
			|| url.startsWith("mailto:")
			|| url.startsWith("mail:")
			|| url.startsWith("javascript:")
			|| url.startsWith("rtsp://");
	}

	private String resolve(String base, String rel) {
		URL absURL = null;
		if(lastBase != null) {
			if(lastBase.equals(base)) {
				try {
					absURL = new URL(baseURL,rel);
				} catch (MalformedURLException e) {
					LOG.warning("Malformed rel url:" + rel);
					return null;
				}
			}
		}
		if(absURL == null) {
			try {
				baseURL = new URL(base);
				lastBase = base;
			} catch (MalformedURLException e) {
				LOG.warning("Malformed base url:" + base);
				return null;
			}
			try {
				absURL = new URL(baseURL,rel);
			} catch (MalformedURLException e) {
				LOG.warning("Malformed rel url:" + rel);
				return null;
			}
		}
		return absURL.toString();
	}
	public String doResolve(String page, String base, String origUrl) {
		if((origUrl == null) || (origUrl.length() == 0)) {
			return null;
		}
		 // fcisneros: we need a cleaner url to avoid horrors like:
		 //http://www.playno1.com/search.php?mod=forum&amp;srchtxt=%E7%9F%9B%E7%9B%BE%E5%A4%A7%E5%B0%8D%E6%B1%BA&amp;formhash=b6c038f7&amp;searchsubmit=true&amp;source=hotsearch
		String url = normalizeUrl(origUrl);
		if(isAbsolute(url)) {
			return url;
		}
		if((base != null) && (base.length() > 0)) {
			String tmp = resolve(base,url);
			if(tmp != null) {
				return tmp;
			}
		}
		if((page != null) && (page.length() > 0)) {
			String tmp = resolve(page,url);
			if(tmp != null) {
				return tmp;
			}
		}
		return origUrl;
	}
	private static String NToStr(Object o) {
		return (o == null) ? null : o.toString();
	}

	private static String normalizeUrl(String url) {
		return removeQueryParams(removeNewLines(url));
	}

	private static String removeNewLines(String url) {
		return url.replaceAll("[\\r\\n]+", "");
	}

	private static String removeQueryParams(String url) {
		int idx = url.lastIndexOf("?");
		if(idx > 0) {
			return url.substring(0, idx);
		}

		return url;
	}
	@Override
	public String exec(Tuple tup) throws IOException {
		// [0] = TARGET-URI of containing page
		// [1] = BASE.href from HTML page, if present
		// [2] = URL (absolute, or server/path relative) found in page
		if(tup == null || tup.size() != 3) {
			return null;
		}

		return doResolve(NToStr(tup.get(0)),
				NToStr(tup.get(1)),NToStr(tup.get(2)));
	}

}
