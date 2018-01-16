package sema_matching;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

public class SemaMatching {
	static int targetThreshold =0;
	static int Othreshold = 1;
	static double exemplarBirth= 1;
	static int threshold = 1;
	//static double normthreshold= 0.97 ;
	static int countTargetComparisons = 0;
	static  ArrayList<String> targetObj =new ArrayList<String>();  //  index of all the strings
	static  ArrayList<String> targetUris = new ArrayList<String>();  // index of all uris
	
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
	    int noExemp=0;
	    int noFitted=0;
	    int pointer=-1;
	    StmtIterator iterin = model.listStatements();
	    //Types of data containers
	    HashMap<Integer, List<Exemplar>> hash = new HashMap<Integer, List<Exemplar>>();
	    
	    while (iterin.hasNext()) {
	    	pointer++;
	        Statement stmt     = iterin.nextStatement();         // get next statement
	        RDFNode subject    = stmt.getSubject();
	        RDFNode object    = stmt.getObject(); 
	        String t = object.toString();
	        String b = subject.toString();
	        t = t.toLowerCase();
	        targetObj.add(t);
	        targetUris.add(b);
	        int index = t.length();
	        targetThreshold = t.length()-(int)Math.floor(exemplarBirth*t.length());
	        List<Exemplar> l = hash.get(index);  //Length based clustering
	        if(l == null){
	        	hash.put(index, l=new ArrayList<Exemplar>());
	        	Exemplar e = new Exemplar(t,pointer); 
	        	l.add(e);
	        	noExemp++;
	        }
	        else{	//Best Fit Approach
	        	int minDist = t.length();
	        	int minIndex = -1;
	        	for (int j = 0 ; j < l.size(); j++){
	        		LevenshteinDistance le = new LevenshteinDistance();
	        		int dist = le.apply(l.get(j).getExemp() , t);
	        		countTargetComparisons++;
        			if( minDist > dist ){
        				minIndex = j;
        				minDist = dist;
        				}
	        	}
	        	if(minDist < index-targetThreshold){
	        		l.get(minIndex).putString(pointer,minDist);
	        		noFitted++;
	        		List<String> m = new ArrayList<String>();
	        		m = targetChunks(t);
	        		l.get(minIndex).putSubStrings(pointer,m);
	        	}
       			else {
      				Exemplar e = new Exemplar(t, pointer); 
       				l.add(e);
       				noExemp++;
        		}
	        }
	    }
	 System.out.println("Number Fitted"+noFitted+"number of exemplars "+ noExemp);
	 System.out.println("state of target dataset "+ hash.size());
	 

     		
	 try ( BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("test.nt"), "utf-8"))) {
	 //processing dedublication
	 
	 Model srcmodel = ModelFactory.createDefaultModel();
	 InputStream insrc = FileManager.get().open(srcfileNameOrUri);
	 if (insrc != null) {
        srcmodel.read(insrc, null, "N-TRIPLE");
        //model.write(System.out, "N-TRIPLE");
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
        source = source.toLowerCase();
        RDFNode subject    = stmt.getSubject();
        String sourceuri = subject.toString();  
                
        int index = source.length();
       
       boolean[] forVeri = new boolean[100000];
       Set<Integer> FromSub = new HashSet<Integer>();
        for(int i=index-threshold;i<=index+threshold; i++){ 
        	List<Exemplar> g = hash.get(i);
        	if(g != null) {
        		//find all candidate exemplars
        		for (int j = 0 ; j < g.size(); j++){
        			Exemplar e = g.get(j);
        			LevenshteinDistance le = new LevenshteinDistance();
	        		int dist = le.apply(e.getExemp() , source);
	        		//System.out.println(dist);
        			if(dist <= threshold){
        				newPairs++;
        				noOfComparisons++;
        				pairsveri++;
        				writer.write(sourceuri  +" <http://www.w3.org/2002/07/owl#sameAs> "+ targetUris.get(e.getPointer()));
        				writer.newLine();
        			}
        			for(int k=dist-threshold;k<=dist+threshold;k++){  //   
        				List<Integer> m = e.getList(k);
        				if(m != null){
        					for(int l=0;l<m.size();l++){
        						noOfComparisons++;
        						forVeri[m.get(l)]=true;
        					}
        				}
        			}
        			int d = i/(threshold+1);
        			List<Integer> m = new ArrayList<Integer>();
        			source = source.toLowerCase();
        			for(int k = 0; k<=source.length() - d;k++){
        				m = e.getShashList(source.substring(k,k+d));
        				if(m!=null){
	        				for(int o = 0; o<m.size();++o){
	        					if(forVeri[m.get(o)]){
	        						
	        						FromSub.add(m.get(o));
	        					}
	        				}
	        			}			
        			}
        			d=d+1;
        			for(int k = 0; k<=source.length()-d;k++){
        				//System.out.println("sub: "+ source.substring(k, k+d) );
        				m = e.getShashList(source.substring(k,k+d));
        				if(m!=null){
        					//System.out.println("Not Once" );
	        				for(int o = 0; o<m.size();++o){
	        					if(forVeri[m.get(o)]){
	        						//System.out.println("found substring" );
	        						FromSub.add(m.get(o));
	        					}
	        				}
	        			}			
        			}
        		}
        	}
        	
        }
        //System.out.println("source: "+ source);
        //System.out.println(FromSub.size());
        newPairs+=FromSub.size();
        Iterator<Integer> it = FromSub.iterator();
        while(it.hasNext()){
        	int intt = it.next();
			String t = targetObj.get(intt);
			String turi = targetUris.get(intt);
			LevenshteinDistance l = new LevenshteinDistance(threshold);
			if(l.apply(t,source)!=-1){
				pairsveri++; 
				writer.write(sourceuri + " <http://www.w3.org/2002/07/owl#sameAs> "+ turi);
				writer.newLine();
				//System.out.println("Didn't Pair  ."+m.get(l)+".  with  ."+source+".");
				}
        }
        							
        				//System.out.println("Inside list of exemplar");			
        				
        			
        	
        
	 }
	 		long total = noOfComparisons+countTargetComparisons;
	 		System.out.println(" total number of comparisons "+ total);
	 		System.out.println("number of comparisons after clustering "+noOfComparisons);
        	System.out.println("Number SubStringContained "+newPairs);
        	System.out.println("Number SubStringContainedVerified "+pairsveri);
        	writer.close();
	 }
        	
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

