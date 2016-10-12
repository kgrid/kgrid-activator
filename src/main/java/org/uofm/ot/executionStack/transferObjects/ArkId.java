package org.uofm.ot.executionStack.transferObjects;

public class ArkId {

    // Fake/sample Ids (for Testing)
    static private final String FAKE_ARKID_PATH = "ark:/99999/12345";
    private String arkId;
    private String naan;
    private String name;

    public ArkId() {
    }

    public ArkId(String path) {

        if (path == null) throw new IllegalArgumentException("Path cannot be null");

        if (path.contains("ark:/")) {
            String[] parts = path.substring("ark:/".length()).split("/");
            naan = parts[0];
            name = parts[1];
        } else {
            if (path.contains("/")) throw new IllegalArgumentException("Non-ark id may not contain slashes");
            this.arkId = this.name = path; // old-style id, e.g. "OT42"
        }
    }

    public static ArkId FAKE_ARKID() {
        return new ArkId(FAKE_ARKID_PATH);
    }

    public String getArkId() {

        if (arkId == null)
            this.arkId = String.format("ark:/%s", naan + "/" + name);
        return arkId;
    }

    public String getFedoraPath() {

        if (naan == null) {
            return name;
        } else {
            return naan + "-" + name;
        }
    }

  

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((naan == null) ? 0 : naan.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArkId other = (ArkId) obj;
		if (naan == null) {
			if (other.naan != null)
				return false;
		} else if (!naan.equals(other.naan))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return getArkId();
    }

    String getNaan() {
        return naan;
    }

//    public void setNaan(String naan) {
//        this.naan = naan;
//    }

    String getName() {
        return name;
    }

    public void setArkId(String arkId) {
        this.arkId = arkId;
    }

    public void setNaan(String naan) {
        this.naan = naan;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    // Status flags for setting state (in external
    // services; ArkId has no status itself)
    // TODO: Maybe move to IdService
    
    
    
    
    public static enum Status {

        PUBLIC("public"),
        RESERVED("reserved"),
        UNAVAILABLE("unavailable");

        private String status;

        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }

    }

}
