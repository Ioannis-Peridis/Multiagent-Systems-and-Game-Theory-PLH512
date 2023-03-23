package org.powertac.samplebroker;

import java.util.LinkedList;
import java.util.Random;

import org.powertac.common.enumerations.PowerType;

public class RevokeNode extends Node {

	public RevokeNode(Node father,double limitPrice, 
						double sigma, int num, PortfolioManagerService pms) {
		super(father,limitPrice, sigma,num, pms);
		super.isThisRevoke=true;
		
	}
	
	@Override
	LinkedList<Node> initializeChildren() {
		return null;
	}
	
	@Override
	float rollout(int depth) {
		float retval=super.heuristic();
		super.calculateNewAverage(retval);
		return retval;
	}
	
	@Override
	Node selectRandomChild() {
		return null;
	}
}
