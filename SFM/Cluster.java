package sema_matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*******************************************************************************
* This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
* To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative * Commons, PO Box 1866, Mountain View, CA 94042, USA.
*
* (c) AI-Group/UNIVERSITY OF PIRAEUS RESEARCH CENTER (UPRC)
*
******************************************************************************/

public class Cluster {
	private HashMap<String, List<Integer>> shash;
	
	public Cluster(){
		this.shash = new HashMap<String, List<Integer>>();
	}
	
	public List<Integer> getShashList(String a){
		if(shash.get(a)!=null){
			//System.out.println("not null case" + a );
			return shash.get(a);
		}
		else {
			//System.out.println("lets see: " + a );
			return null;
		}
	}
	
	public void putSubStrings(int point, List<String> l){
        for(int i=0;i<l.size();++i){
        	String s= l.get(i);
        	List<Integer> m = shash.get(s);
        	if(m == null)
               	shash.put(s, m=new ArrayList<Integer>());
            m.add(point);
        }
	}

}
