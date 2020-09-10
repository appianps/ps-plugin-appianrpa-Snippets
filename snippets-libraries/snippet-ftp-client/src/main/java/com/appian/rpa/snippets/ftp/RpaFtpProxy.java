package com.appian.rpa.snippets.ftp;

public class RpaFtpProxy {

	String proxyHost;
	String proxyPort;
	String proxyUser;
	String proxyPassword;

	public RpaFtpProxy(String proxyHost, String proxyPort) {

		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public RpaFtpProxy(String proxyHost, String proxyPort, String proxyUser, String proxyPassword) {

		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
	}

//	public boolean connectProxy(String urlTest) {
//
//		try {
//			DataInputStream di = null;
//			FileOutputStream fo = null;
//			byte[] b = new byte[1];
//
//			// PROXY
//			System.setProperty("http.proxyHost", "proxy.mydomain.local");
//			System.setProperty("http.proxyPort", "80");
//
//			Authenticator.setDefault(new Authenticator() {
//				@Override
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication("mydomain\\username", "password".toCharArray());
//				}
//			});
//
//			URL u = new URL(urlTest);
//			HttpURLConnection con = (HttpURLConnection) u.openConnection();
//			di = new DataInputStream(con.getInputStream());
//			while (-1 != di.read(b, 0, 1)) {
//				System.out.print(new String(b));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//	}

}
