package nl.progaia.esbprocessdraw.draw.esb;

import nl.progaia.esbprocessdraw.draw.Drawable;
import nl.progaia.esbprocessdraw.draw.VerticalDrawable;
import nl.progaia.esbprocessdraw.schema.EndpointRefTypeEnumeration;
import nl.progaia.esbprocessdraw.schema.FanoutType;
import nl.progaia.esbprocessdraw.schema.ItineraryType;
import nl.progaia.esbprocessdraw.schema.StepType;
import nl.progaia.esbprocessdraw.schema.ItineraryType.Decision;

public class ESBItinerary extends VerticalDrawable {

	public ESBItinerary(ItineraryType itinerary) {
		super();
		
		// Walk the itinerary and create child components
		for(Object item: itinerary.getStepOrFanoutOrDecision()) {
			if(item instanceof StepType) {
				add(createStep((StepType)item));
			} else 
			if(item instanceof FanoutType) {
				add(createFanout(((FanoutType)item)));				
			} else
			if(item instanceof Decision) {
				add(createDecision((Decision)item));
			} else {
				System.out.println("Unknown itinerary item: " + item);
			}
		}		
	}
	
	private Drawable createDecision(Decision decision) {
		return new ESBDecision(decision);
	}

	private Drawable createFanout(FanoutType fanout) {		
		return new ESBFanout(fanout);
	}

	private Drawable createStep(StepType step) {
		if(step.getType() == EndpointRefTypeEnumeration.PROCESS) {
			return ESBProcess.getProcess(step.getEndpointRef());
		} else {
			return new ESBStep(step);
		}
	}
}
