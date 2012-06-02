package jade.core;

class VersionManager {
	
	public String getVersion() {
		String version = "4.1.1"; // The $ surrounded Version keyword is automatically replaced by the target doTag of build.xml
		return version;
	}
	
	public String getRevision() {
		String revision = "6532"; // The $ surrounded WCREV keyword is automatically replaced by WCREV with subversion
		return revision;
	}
	
	public String getDate() {
		String date = "2011/11/18 16:21:34"; // The $ surrounded WCDATE keyword is automatically replaced by WCREV with subversion
		return date;
	}
}
