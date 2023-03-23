package org.powertac.samplebroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.powertac.common.TariffSpecification;
import org.powertac.common.enumerations.PowerType;

public class Node {
	PortfolioManagerService pms;
	Node father;
	float averageReward;
	int n; //stands for the times visiting THIS node

	LinkedList<Node> children;

	
	double limitPrice; //represent the value of the bid in this current node
	double sigma;
	int numOfChildren; //stands for the how man sigma i am gona have
	boolean isThisRevoke;
	//ex. if we need 7 children the numOfChild=3 and the code will produce 2*3+1 children

	public Node(Node father,double limitPrice, double sigma, int num, PortfolioManagerService pms) {
		this.isThisRevoke=false;
		this.averageReward=0;
		this.n=1;
		this.limitPrice=limitPrice;
		this.sigma=sigma;
		this.numOfChildren=num;
		this.children=new LinkedList<Node>();
		this.pms=pms;
		this.father=father;

	}

	void increaseN() {
		this.n++;
	}

	/* 
	 * N stand for total iterations of the MC algorithm 
	 * c is a weight for exploitation vs exploration
	 * This might be changed (reduces) as the iterations gets bigger 
	 * 
	 * */
	float UCT(float c, int N) {
		return (float)(this.averageReward+2*c*Math.log(N/this.n));
	}

	LinkedList<Node> initializeChildren() {
		LinkedList<Node> retval=new LinkedList<Node>();
		for(int i=0; i<2*this.numOfChildren+2; i++) 
			retval.add(i, new Node(this,limitPrice, sigma, numOfChildren, this.pms)); //this might change cause the children maybe will have less children
		return retval;
	}

	void calculateNewAverage(float newPrice) {
		this.averageReward=(this.averageReward*(this.n-1)+newPrice)/n;
	}

	float rollout(int depth) {
		Node begin=this;
		Node current=this;
		while (depth>0) {
			Node child=current.selectRandomChild();
			if(child==null) break;
			current=child;
			depth--;
		}
		float retval=current.heuristic();
		begin.calculateNewAverage(retval);
		return retval;
	}
	/* the criteria on which is based the heuristic are:
	 * */

	static double consumptionUsage=0.74; //magic number

	float heuristic() {
		int ourTotalCustomers;
		int totalCustomers=67510; 
		double customerPercentage;
		int evaluation=0;

		if(pms.competingTariffs.isEmpty()) System.out.println("is empty");
		List<TariffSpecification> competing=pms.getAllCompetingTariffs();
		if (competing==null) {
			System.out.println("there is no other tarrifs");
			return 5; //completely random i have to check it
		}

		double minTariff=this.minimumTariff(competing); //is working

		HashMap<String, Integer> customers=pms.customGetCustomerCounts(); 
		List<Integer> totalCust=new ArrayList<Integer>(customers.values());

		int tmp=0;
		for (int t: totalCust) {
			tmp+=t;
		}
		ourTotalCustomers=tmp;
		
		customerPercentage=ourTotalCustomers*100/totalCustomers;
		double custDiff=customerPercentage-60;//cutsomerPercentage - percentage of ideal customer usage
		
		double priceDiff=(this.limitPrice-minTariff)*100/minTariff;//
		int smallCustDiff=10;
		int smallPriceDiff=10;
		
		if(Math.abs(custDiff)<=smallCustDiff/2) {
			if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=5;
			else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=5;
			else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=4;
			else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=4;
			else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=3;
			else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff) evaluation=3;
			else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff) evaluation=2;
			else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff) evaluation=2;
			else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff) evaluation=1;
			else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff) evaluation=1;
			else if(priceDiff>5*smallPriceDiff){
				if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
			} 
			else if(priceDiff<-5*smallPriceDiff){
				if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
			}
			
		}else if(Math.abs(custDiff)<=smallCustDiff*1.5 && Math.abs(custDiff)>smallCustDiff/2) {
			if(custDiff>=0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=4;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=4;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=5;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=3;
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=3;
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff) evaluation=2;
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff) evaluation=2;
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff) evaluation=1;
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff) evaluation=1;
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}	
			}else if(custDiff<0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=4;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=5;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=3;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=4;
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=2;
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff) evaluation=3;
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff) evaluation=1;
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff) evaluation=2;
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff) evaluation=1;
				else if(priceDiff>5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
			}
		}else if(Math.abs(custDiff)<=smallCustDiff*2.5 && Math.abs(custDiff)>smallCustDiff*1.5) {
			if(custDiff>=0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=3;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=2;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=4;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=1;
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=5;
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff) evaluation=3;
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff) evaluation=2;
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>5*smallPriceDiff) evaluation=1;
				else if(priceDiff<-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
			}else if(custDiff<0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=3;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=3;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=2;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=4;
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=1;
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff) evaluation=5;
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
				}
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff) evaluation=4;
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
				}
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff) evaluation=3;
				else if(priceDiff>5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
				}
				else if(priceDiff<-5*smallPriceDiff) evaluation=2;
			}
		}else if(custDiff<=smallCustDiff*4||custDiff>=smallCustDiff*(-6) && Math.abs(custDiff)>smallCustDiff*2.5) {
			if(custDiff>=0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=2;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=1;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=3;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
				evaluation=0;
				}
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff) evaluation=4;
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff) evaluation=5;
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff) evaluation=2;
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff>5*smallPriceDiff) evaluation=1;
				else if(priceDiff<-5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
			}else if(custDiff<0) {
				if(priceDiff>0 && priceDiff<smallPriceDiff) evaluation=1;
				else if(priceDiff<0 && priceDiff>-smallPriceDiff) evaluation=2;
				else if(priceDiff>smallPriceDiff && priceDiff<2*smallPriceDiff) evaluation=1;
				else if(priceDiff<-smallPriceDiff && priceDiff>-2*smallPriceDiff) evaluation=2;
				else if(priceDiff>2*smallPriceDiff && priceDiff<3*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-2*smallPriceDiff && priceDiff>-3*smallPriceDiff) evaluation=3;
				else if(priceDiff>3*smallPriceDiff && priceDiff<4*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-3*smallPriceDiff && priceDiff>-4*smallPriceDiff) evaluation=4;
				else if(priceDiff>4*smallPriceDiff && priceDiff<5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-4*smallPriceDiff && priceDiff>-5*smallPriceDiff) evaluation=5;
				else if(priceDiff>5*smallPriceDiff){
					if(this.isThisRevoke) return (float) (6*Math.random());
					evaluation=0;
				}
				else if(priceDiff<-5*smallPriceDiff) evaluation=5;
			}
		}

		return evaluation;    	 

	}

	Node selectRandomChild(){
		if(this.children.size()==0) return null;
		this.children=initializeChildren();
		Random r=new Random();
		int size=this.children.size();
		int a=r.nextInt(size);
		return this.children.get(a);
	}

	int maxAverageChild(){
		
		float a=Float.MIN_VALUE;
		int offset=-1;
		for(int i=0; i<this.children.size(); i++) { //now children size has the correct  size
			Node n=this.children.get(i);
			if(n.averageReward>a) {
				a=n.averageReward;
				offset=i;
			}
		}
		return offset;
	}
	
	Node ithMaxAverageChild(int i) {
		int j=0;
		Node tmp=this;
		while(j<i) {
			tmp=tmp.maxAverageChildNode(false);
			System.out.println("----------------");
			if (tmp==null)return null;
			j++;
		}
		return tmp.maxAverageChildNode(false);
	}
	

	Node maxAverageChildNode(boolean print) {
		int i=-1;
		int z=0;
		Node retval= new Node(this.father, 0.0,0.0,0,null);
		float min=-Float.MAX_VALUE;
		for(Node tmp: this.children) {
			if(print) System.out.println(tmp.averageReward);
			i++;
			if(tmp.averageReward>min) {
				z=i;
				retval=tmp;
				min=tmp.averageReward;
			}
		}
		//if(z==0) return null; //uncomment for revoke
		return retval;
	}

	void backpropagate(float price) {
		if(this.father==null) return ;
		this.father.calculateNewAverage(price);
	}


	//miscellaneous

	double minimumTariff(List<TariffSpecification> list) {

		if (list.isEmpty()) {
			System.out.println("list is empty");
			return 0.0;
		}
		double minimum=Double.MAX_VALUE;
		for(TariffSpecification spec:list) {
			double value=spec.getRates().get(spec.getRates().size()-1).getValue();
			if(value<minimum) {
				minimum=value;
			}
		}

		return minimum;
	}

}
