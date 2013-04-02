package ca.parcplace.birdsong.android;

class BirdServiceResponse {

	// the webservice responds with:
	// xeno-canto number; genus; species; english; subspecies; recordist; country; location; latitude; longitude; songtype
	private String genus;
	private String species;
	private String englishName;
	private String subspecies;
	public String getGenus() {
		return genus;
	}
	
	public void setGenus(String genus) {
		this.genus = genus;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
	public String getEnglishName() {
		return englishName;
	}
	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}
	public String getSubspecies() {
		return subspecies;
	}
	public void setSubspecies(String subspecies) {
		this.subspecies = subspecies;
	}

	@Override
	public String toString() {
		return "BirdServiceResponse [genus=" + genus + ", species=" + species
				+ ", englishName=" + englishName + ", subspecies=" + subspecies
				+ "]";
	}
	
	
}
