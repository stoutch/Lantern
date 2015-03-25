package com.example.deept_000.masproject.Gson;

/**
 * Created by Chris on 3/25/2015.
 */
public class Heatmap {
    private boolean success;
    public Response[] response;

    public class Response {
        public Loc loc;
        public int weight;
        public int value;
        public long lighting_index;
        public String type;
        public String day;

        public class Loc {
            public String type;
            public double[] coordinates;
        }
    }

    public String toString() {
        String result = "";
        result += "success: " + success + "\n";
        for (Response r : response) {
            result += "loc: " + r.loc + "\n";
            result += "weight: " + r.weight + "\n";
            result += "value: " + r.value + "\n";
        }
        return result;
    }
}

