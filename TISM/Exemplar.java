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

public class Exemplar{
	private String exemp;
	private int pointer;
	private HashMap<Integer, List<Integer>> hash;
	private HashMap<String, List<Integer>> shash;
	
	
	public Exemplar(String a, int pointer){
		this.exemp = a;
		this.pointer=pointer;
		this.hash = new HashMap<Integer, List<Integer>>();
		this.shash = new HashMap<String, List<Integer>>();
	}
	//accessor methods
	public String getExemp(){
		return exemp;
	}
	public List<Integer> getList(int a){
		return hash.get(a);
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
	
	public int getSize(){
		return hash.size();
	}
	public int getPointer(){
		return pointer;
	}
	//mutator methods
	public void setExemp(String exemp){
		this.exemp = exemp; 
	}
	public void putString(int a,int index){
        List<Integer> l = hash.get(index);
        if(l == null)
        	hash.put(index, l=new ArrayList<Integer>());
        l.add(a);
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
		
     
	public void printExemp(){
		System.out.println("the exemplar is:"+ this.getExemp());
		for(int i=0;i<this.getExemp().length()-2;i++){
			List<Integer> l = hash.get(i);
			if(l!=null)
				for (int j = 0 ; j < l.size(); j++){
					System.out.println(l.get(j));
				}
		}
	}
	
	
}
