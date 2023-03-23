package org.powertac.samplebroker;

import org.powertac.common.enumerations.PowerType;

public class MCTS {
	//this might 
	Node root; //watch out root has a sigma and a limit price

	int N; //stands for the total iterations
	float c;// weights the exploration
	boolean startRollout;

	public MCTS(Node r) {
		this.root=r;
		this.N=0;
		this.c=(float) Math.sqrt( 2.0);
		this.startRollout=false;
	}
	
	void nextIteration() {
		this.N++;
	}
	
	Node selectNext(Node current) {
		if(current.children.size()<(2*current.numOfChildren)+2) { //for revoke +2
			int i=current.children.size(); //we have to store which child the next one is (ex first, second... bid)
//			if(current.limitPrice==0 && i!=0)
//				System.out.println("break");
			double newLimitPrice=current.limitPrice+
					(i-1-current.numOfChildren)*(current.sigma); //IF WE WANT TO REVOKE I-1
			Node n;
			if(i==0) {//Revoke Node
				n= new RevokeNode(current, current.limitPrice, current.sigma/4, current.numOfChildren, current.pms);
			}else {
				n=new Node(current, newLimitPrice, current.sigma/4, current.numOfChildren, current.pms);
			}
			current.children.add(n);
			this.startRollout=true;
			return n;
		}
		float max=Float.MIN_VALUE;
		Node retval=new Node(null, (float)0.0, 0,0, current.pms);
		for(int j=0; j<current.children.size(); j++) {
			Node child=current.children.get(j);
			float tmp=child.UCT(c,N);
			if(tmp>max) {
				max=tmp;
				retval=child;
			}
		}
		return retval;
	}

	/* 
	 * Returns the value of the rollout/heuristic in 
	 * order to backpropagate 
	 * 
	 * first call:
	 * current is going to be a root and depth is going to be 24
	 * 
	 * */

	Node treeSearch(int depth) { 
		Node current=this.root; 
		while(depth>=0) { 
			Node next=this.selectNext(current);
			next.increaseN(); 
			
			if (this.startRollout || depth==1) {
				this.startRollout=false; 
				float price=next.rollout(depth);
				next.backpropagate(price); 
				Node back= next.father; 
				while(back!=null) {
					back.backpropagate(price);
					back=back.father; 
				} 
				return this.root; 
			} 
			depth--;
			
			current=next;

		}
		return this.root;


	}


	
}
