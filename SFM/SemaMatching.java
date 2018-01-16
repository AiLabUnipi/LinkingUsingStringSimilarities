package sema_matching;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.commons.text.similarity.LevenshteinDistance;

/*******************************************************************************
* This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
* To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative * Commons, PO Box 1866, Mountain View, CA 94042, USA.
*
* (c) AI-Group/UNIVERSITY OF PIRAEUS RESEARCH CENTER (UPRC)
*
******************************************************************************/

public class SemaMatching { //only indexing method per cluster
	
	static int threshold = 3;
	static int countTargetComparisons = 0;
	static  ArrayList<String> targetObj =new ArrayList<String>();  //  index of all the strings
	static  ArrayList<String> targetUris = new ArrayList<String>();  // index of all uris
	static ArrayList<int[]> freq = new ArrayList<int[]>();
	static  Cluster[] clusters = new Cluster[50];
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		String fileNameOrUri = "hugethirtyTofourty.nt";
		String srcfileNameOrUri = "hugethirtyTofourty.nt";
		int newPairs = 0;
		int pairsveri = 0;
	
	    Model model = ModelFactory.createDefaultModel();
	    InputStream in = FileManager.get().open(fileNameOrUri);
	    if (in != null) {
	        model.read(in, null, "N-TRIPLE");
	    } else {
	        System.err.println("cannot read " + fileNameOrUri);
	    }
	    
	    int pointer=-1;
	    StmtIterator iterin = model.listStatements();
	    double sum=0;
	    while (iterin.hasNext()) {
	    	pointer++;
	        Statement stmt     = iterin.nextStatement();         // get next statement
	        RDFNode subject    = stmt.getSubject();
	        RDFNode object    = stmt.getObject(); 
	        String t = object.toString();
	        t = t.toLowerCase();
	        String b = subject.toString();
	        int[] f = new int[150];
	        f = findFreq(t);
	        targetObj.add(t);
	        freq.add(f);
	        targetUris.add(b);
	        int index = t.length();
	        sum=sum+t.length();
	        List<String> m = new ArrayList<String>();
	        m = targetChunks(t);
	        if(clusters[index]==null)
	        	clusters[index] = new Cluster();
	        clusters[index].putSubStrings(pointer,m);
	    }
	
	 try ( BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("test.nt"), "utf-8"))) {
	 //processing dedublication
	 
	 Model srcmodel = ModelFactory.createDefaultModel();
	 InputStream insrc = FileManager.get().open(srcfileNameOrUri);
	 if (insrc != null) {
        srcmodel.read(insrc, null, "N-TRIPLE");
	 } else {
        System.err.println("cannot read " + srcfileNameOrUri);
	 }

	 StmtIterator itersrc = srcmodel.listStatements();
	 //int noOfSourceTriples=0;
	 long noOfComparisons=0;
	 while (itersrc.hasNext()) {
		//noOfSourceTriples++;
        Statement stmt     = itersrc.nextStatement();         // get next statement     	 
        RDFNode object    = stmt.getObject(); 
        String source = object.toString();
        RDFNode subject    = stmt.getSubject();
        String sourceuri = subject.toString();
        source = source.toLowerCase();
        int[] fsource = new int[200];
        fsource = findFreq(source);
        
        int index = source.length();
       
       Set<Integer> FromSub = new HashSet<Integer>();
        for(int i=index-threshold;i<=index+threshold; i++){ 
        	int d = i/(threshold+1);
        	for(int k = 0; k<=source.length() - d;k++){
        		List<Integer> m = new ArrayList<Integer>();
        		if(clusters[i]!=null)
        			m = clusters[i].getShashList(source.substring(k,k+d));
        		if(m!=null){
        			for(int o = 0; o<m.size();++o){
        				FromSub.add(m.get(o));
        			}
        		}
        	}			
        	d=d+1;
        	for(int k = 0; k<=source.length()-d;k++){
        		List<Integer> m = new ArrayList<Integer>();
        		if(clusters[i]!=null)
        			m = clusters[i].getShashList(source.substring(k,k+d));
        		if(m!=null){
        			for(int o = 0; o<m.size();++o){
        					FromSub.add(m.get(o));
        			}
        		}
        	}
        }     
       
        newPairs+=FromSub.size();
        Iterator<Integer> it = FromSub.iterator();
        while(it.hasNext()){
        	int intt = it.next();
			String t = targetObj.get(intt);
			String turi = targetUris.get(intt);
			int d_l =Math.abs(t.length()-source.length());
			if(threshold>0)
					if(!compare(freq.get(intt), fsource, threshold,d_l)) continue;
			noOfComparisons++;
			LevenshteinDistance l = new LevenshteinDistance(threshold);
			if(l.apply(t,source)!=-1){
				pairsveri++; 
				writer.write(sourceuri + " <http://www.w3.org/2002/07/owl#sameAs> "+ turi);
				writer.newLine();
				//System.out.println("Didn't Pair  ."+m.get(l)+".  with  ."+source+".");
			}
        }
	 }
	 writer.close();
	System.out.println("number of comparisons after clustering "+noOfComparisons);
 	System.out.println("average length" + sum/100000);
 	System.out.println("Number SubStringContainedVerified "+pairsveri);
	}
	 		
        	
	 }
        	
	private static boolean compare(int[] is, int[] fsource, int t, int d_l) {
		// TODO Auto-generated method stub
		int diff=0;
		for(int i=97; i<122; ++i){
			diff+=Math.abs(is[i]-fsource[i]);
			if(diff>2*t-d_l)return false;
		}
		return true;
	}

	//endmain
	public static int[] findFreq(String t){
		t = t.toLowerCase();
		int[] f = new int[150];
		for(int i=0;i<t.length();++i)
			if((int)t.charAt(i)<150) f[(int)t.charAt(i)]++;
		return f;
	}
	
	public static List<String> targetChunks(String t){
		List<String> l = new ArrayList<String>();
		t = t.toLowerCase();
		int d = t.length()/(threshold+1);
		int count = t.length()%(threshold+1);
		if(d!=0){
			int i = 0;
			for(i =0; i < (threshold+1)-count; i++)
			{
			     l.add(t.substring(i*d, (i+1)*d));
			        
			}
		    for(i = (threshold+1)-count ; i < (threshold+1) ; i++)
		    {
		        l.add(t.substring(i*d, (i+1)*d+1));
		    }
		    
		}else{
			l.add(t);
		}
		return l;	 
	}
	
	public static void printChunks(List<String> l){
		for(int i=0;i<l.size();++i)
				System.out.println(l.get(i));
	}
	
	public static boolean stringContainsSub(String s, String t)
	{
		s = s.toLowerCase();
        t = t.toLowerCase();
		int d = t.length()/(threshold+1);
		int count = t.length()%(threshold+1);
		if(d!=0){
			int i = 0;
			for(i =0; i < (threshold+1)-count; i++)
			{
			     if(s.contains(t.substring(i*d, (i+1)*d-1)))
			        {
			    	 return true;
			        }
			}
		    for(i = (threshold+1)-count ; i < (threshold+1) ; i++)
		    {
		        if(s.contains(t.substring(i*d, (i+1)*d)))
		        {
		            return true;
		        }
		    }
		}
		else{
			return true;

		}
	    return false;
	}
}
