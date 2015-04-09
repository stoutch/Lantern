package com.example.deept_000.masproject.Gson;

/**
 * Created by Chris on 4/8/2015.
 */
public class Routes {
    public boolean success;
    public Response response;

    public class Response {
        public Route[] routes;
        public double[] score;
        public long[] route_index;

        public class Route {
            public Leg[] legs;

            public class Leg {
                public Step[] steps;
                public LegDuration duration;

                public class LegDuration {
                    public String text;
                    public int value;
                }

                public class Step {
                    public RouteLocation start_location;
                    public RouteLocation end_location;

                    public class RouteLocation {
                        public double lat;
                        public double lng;
                    }
                }

            }
        }
    }

    public String toString() {
        String result = "";
        result += "success: " + success + "\n";
        if (response == null) {
            return result;
        }
        for (Response.Route r : response.routes) {
            result += "Route: \n";
            if (r.legs != null) {
                result += "legs: \n";
                for (Response.Route.Leg leg : r.legs) {
                    if (leg.duration != null) {
                        result += "Duration: " + leg.duration.text + "\n";
                    } else {
                        result += "Duration: null\n";
                    }
                    if (leg.steps != null) {
                        for (Response.Route.Leg.Step step : leg.steps) {
                            result += "steps: \n";
                            result += "start: " + step.start_location.lat + ", " + step.start_location.lng + "\n";
                            result += "end: " + step.end_location.lat + ", " + step.end_location.lng + "\n";
                        }
                    } else {
                        result += "steps: null\n";
                    }
                }
            } else {
                result += "leg: null\n";
            }
        }
        return result;
    }
}
