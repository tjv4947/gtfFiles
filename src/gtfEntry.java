public class gtfEntry {
	// object to hold generalized FASTQ formatted data
	public String chromosomeID ;
	public String sourceType ;
	public String expressionType ;
	public boolean isIntron = false ;
	public long startCodon = 0 ;
	public long stopCodon = 0 ;
	public String strand  ;
	public String gene_id ;
	public int transcript_id ;
	public int exon_number ;
	public int exon_id ;
	public int gene_name ;
	public String stubString ;  /// remove when parsing completed
	
	

	public  gtfEntry () {
		// stub of an instantiation
	}
	
	public  gtfEntry (String inpChromosomeID, String sourceType, String expressionType, long inpStartCodon, long inpStopCodon, boolean inpIsIntron, String strand, String inpStubString) {
		this.chromosomeID = inpChromosomeID ;
		this.sourceType = sourceType ;
		this.expressionType = expressionType;
		this.startCodon = inpStartCodon ;
		this.stopCodon = inpStopCodon ;
		this.isIntron = inpIsIntron ;
		this.strand = strand ;
		this.stubString = inpStubString ;
	}

}
