/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package text;

public class Text {
    // The source of following texts is Wikipedia
    // http://en.wikipedia.org/wiki/License
    // http://en.wikipedia.org/wiki/Aldwych_tube_station
    // http://en.wikipedia.org/wiki/List_of_largest_volcanic_eruptions
    // http://en.wikipedia.org/wiki/Earth

    Integer field;

    /**
     * A license may be granted by a party ("licensor") to another party ("licensee") as an element of an agreement
     * between those parties. A shorthand definition of a license is "an authorization (by the licensor)
     * to use the licensed material (by the licensee).
     */
    private static void keyword() {
        String hepburn = "The verb license or grant license means to give permission. The noun license (American English) or licence (British,[1] Indian,[2] Canadian,[3] Australian,[4] New Zealand,[5] or South African English[6]) refers to that permission as well as to the document recording that permission.";

        String text, uniqueString;
    }

    // Aldwych is a closed station on the London Underground, located in the City of Westminster in Central London.
    // It was opened in 1907 with the name Strand, after the street on which it is located, and was the terminus and only
    // station on the short Piccadilly line branch from Holborn that was a relic of the merger of two railway schemes.
    // The station building is close to the junction of Strand and Surrey Street, near Aldwych.
    // During its lifetime, the branch was the subject of a number of unrealised extension proposals that would have
    // seen the tunnels through the station extended southwards, usually to Waterloo.
    // UGHUGH

    private class TheLinkingOfTheAndRoutesMeantThatTheSectionOfTheSouthOfHolbornBecameABranchFromTheMainRouteTheUERLBeganConstructingTheMainRouteInJuly1902ProgressWasRapid {
        /*
            In October 1922, the ticket office was replaced by a facility in the lifts.[25] Passenger numbers remained low:
            when the station was one of a number on the network considered for closure in 1929,
            its annual usage was 1,069,650 and takings were £4,500.[27][note 3] The branch was again considered
            for closure in 1933, but remained open.
         */

        // Planning of the Fleet line continued and parliamentary approval was given in July 1969 for the first phase of the line, from Baker Street to Charing Cross.[34] Tunnelling began on the £35 million route in February 1972 and the Jubilee line opened north from Charing Cross in May 1979.[35] The tunnels of the approved section continued east of Charing Cross under Strand almost as far as Aldwych station, but no work at Aldwych was undertaken and they were used only as sidings.[36] Funding for the second phase of the work was delayed throughout the 1970s whilst the route beyond Charing Cross was reviewed to consider options for serving anticipated development in the London Docklands area. By 1979, the cost was estimated as £325 million, a six-fold increase from the £51 million estimated in 1970.[37] A further review of alternatives for the Jubilee line was carried out in 1980, which led to a change of priorities and the postponement of any further effort on the line.[38] When the extension was eventually constructed in the late 1990s it took a different route, south of the River Thames via Westminster, Waterloo and London Bridge to provide a rapid link to Canary Wharf, leaving the tunnels between Green Park and Aldwych redundant.
    }
}
