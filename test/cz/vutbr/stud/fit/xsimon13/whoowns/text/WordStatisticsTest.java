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

package cz.vutbr.stud.fit.xsimon13.whoowns.text;

import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WordStatisticsTest {

    private WordStatistics idf;

    @Before
    public void setup() throws Exception {
        idf = new WordStatistics(TestUtils.getTestJedis());
        idf.clear();
    }

    @After
    public void tearDown() throws Exception {
        idf.clear();
    }

    @Test
    public void languageTest() throws Exception {
        String[] documents = new String[] {
                // The source of following text is Wikipedia
                // http://en.wikipedia.org/wiki/License
                // http://en.wikipedia.org/wiki/Aldwych_tube_station
                // http://en.wikipedia.org/wiki/List_of_largest_volcanic_eruptions
                // http://en.wikipedia.org/wiki/Earth

                "The verb license or grant license means to give permission. The noun license (American English) or licence (British,[1] Indian,[2] Canadian,[3] Australian,[4] New Zealand,[5] or South African English[6]) refers to that permission as well as to the document recording that permission.",
                "A license may be granted by a party (\"licensor\") to another party (\"licensee\") as an element of an agreement between those parties. A shorthand definition of a license is \"an authorization (by the licensor) to use the licensed material (by the licensee).\"",
                "Aldwych is a closed station on the London Underground, located in the City of Westminster in Central London. It was opened in 1907 with the name Strand, after the street on which it is located, and was the terminus and only station on the short Piccadilly line branch from Holborn that was a relic of the merger of two railway schemes. The station building is close to the junction of Strand and Surrey Street, near Aldwych. During its lifetime, the branch was the subject of a number of unrealised extension proposals that would have seen the tunnels through the station extended southwards, usually to Waterloo.",
                "The linking of the GN&SR and B&PCR routes meant that the section of the GN&SR south of Holborn became a branch from the main route. The UERL began constructing the main route in July 1902. Progress was rapid, so that it was largely complete by the Autumn of 1906.[13] Construction of the Holborn to Strand section was delayed while the London County Council constructed Kingsway and the tramway subway running beneath it and while the UERL decided how the junction between the main route and the branch would be arranged at Holborn.",
                "In October 1922, the ticket office was replaced by a facility in the lifts.[25] Passenger numbers remained low: when the station was one of a number on the network considered for closure in 1929, its annual usage was 1,069,650 and takings were £4,500.[27][note 3] The branch was again considered for closure in 1933, but remained open.",
                "Planning of the Fleet line continued and parliamentary approval was given in July 1969 for the first phase of the line, from Baker Street to Charing Cross.[34] Tunnelling began on the £35 million route in February 1972 and the Jubilee line opened north from Charing Cross in May 1979.[35] The tunnels of the approved section continued east of Charing Cross under Strand almost as far as Aldwych station, but no work at Aldwych was undertaken and they were used only as sidings.[36] Funding for the second phase of the work was delayed throughout the 1970s whilst the route beyond Charing Cross was reviewed to consider options for serving anticipated development in the London Docklands area. By 1979, the cost was estimated as £325 million, a six-fold increase from the £51 million estimated in 1970.[37] A further review of alternatives for the Jubilee line was carried out in 1980, which led to a change of priorities and the postponement of any further effort on the line.[38] When the extension was eventually constructed in the late 1990s it took a different route, south of the River Thames via Westminster, Waterloo and London Bridge to provide a rapid link to Canary Wharf, leaving the tunnels between Green Park and Aldwych redundant.",
                "There have probably been many such eruptions during Earth's history beyond those shown in these lists. However erosion and plate tectonics have taken their toll, and many eruptions have not left enough evidence for geologists to establish their size. Even for the eruptions listed here, estimates of the volume erupted can be subject to considerable uncertainty.",
                "Earth, also called the world[n 4] and, less frequently, Gaia[n 5] (and Terra in some works of science fiction[27]) is the third planet from the Sun, the densest planet in the Solar System, the largest of the Solar System's four terrestrial planets, and the only astronomical object known to accommodate life. The earliest life on Earth arose at least 3.5 billion years ago.[28][29][30] Earth's biodiversity has expanded continually except when interrupted by mass extinctions.[31] Although scholars estimate that over 99 percent of all species that ever lived on the planet are extinct,[32][33] Earth is currently home to 10–14 million species of life,[34][35] including over 7.3 billion humans[36] who depend upon its biosphere and minerals. Earth's human population is divided among about two hundred sovereign states which interact through diplomacy, conflict, travel, trade and communication media.",
                "Estimates on how much longer the planet will be able to continue to support life range from 500 million years (myr), to as long as 2.3 billion years (byr).[70][71][72] The future of the planet is closely tied to that of the Sun. As a result of the steady accumulation of helium at the Sun's core, the star's total luminosity will slowly increase. The luminosity of the Sun will grow by 10% over the next 1.1 byr and by 40% over the next 3.5 byr.[73] Climate models indicate that the rise in radiation reaching Earth is likely to have dire consequences, including the loss of the planet's oceans."
        };

        for (String document : documents)
            idf.analyzeDocument(document);

        makeAssertions(documents);

        idf.save();
        idf = new WordStatistics(TestUtils.getTestJedis());

        makeAssertions(documents);
    }

    private void makeAssertions(String[] documents) throws Exception {
        Assert.assertEquals(idf.getDocumentCount(), documents.length);

        Assert.assertTrue(idf.getIdfFw("aldwych") > idf.getIdfFw("the"));
        Assert.assertTrue(idf.getIdfFw("aldwych") > idf.getIdfFw("was"));
        Assert.assertTrue(idf.getIdfFw("history") > idf.getIdfFw("was"));
        Assert.assertTrue(idf.getIdfFw("science") > idf.getIdfFw("a"));
    }
}
