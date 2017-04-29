module nl.ordina.jtech.jdk9.http2 {
	requires java.base;
	requires java.xml.bind;
	requires java.logging;
	
	//requires java.httpclient;
	// replaced by:
	requires jdk.incubator.httpclient;
}