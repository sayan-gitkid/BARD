/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bard.core.rest.spring.compound

import bard.core.rest.spring.compounds.CompoundAnnotations
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CompoundAnnotationsUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    public static final String COMPOUND_ANNOTATIONS = '''
    {
       "anno_key":[
          "CompoundSpectra",
          "DOCUMENTS",
          "DOCUMENTS",
          "CompoundUNII",
          "CompoundCAS",
          "CompoundCAS",
          "CompoundCAS",
          "CompoundCAS",
          "CompoundIndication",
          "CompoundIndication",
          "CompoundIndication",
          "CompoundDrugLabelRx",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "COLLECTION",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "Synonyms",
          "CompoundMOA",
          "CompoundMOA"
       ],
       "anno_val":[
          "http://tripod.nih.gov/npc/spectra/NCGC00095264.png",
          "1820340",
          "7513790",
          "2I8BD50I8B",
          "773-76-2",
          "8067-69-4",
          "8021-96-3",
          "81117-07-9",
          "Dermatologic",
          "Antiseptic",
          "Used in the treatment of dandruff and mild to moderately severe seborrheic dermatitis of the scalp.",
          "0072-6850;CAPITROL;shampoo;chloroxine;20 mg in 1 g;2I8BD50I8B",
          "NPC informatics|NPC-7244033|ChemDiv, Inc:3406-0528|Microsource:MS-1503202",
          "HTS amenable drugs",
          "Approved drugs",
          "FDA orange book",
          "FDA drugs@FDA",
          "FDA NDC",
          "KEGG",
          "Human approved drugs",
          "FDA approved",
          "FDA maximum daily dose",
          "FDA human approved",
          "DrugBank v3.0|DB01243",
          "FDA DailyMed",
          "NPC screening|NCGC00095264",
          "QC spectra",
          "Quixalin",
          "Capitrol",
          "Dichloroquine",
          "5,7-Dichloro-8-quinolinol",
          "Dichlorohydroxyquinoline",
          "Chloroxine",
          "Chlorhydroxyquinoline",
          "Chloroxine hydrofluoride",
          "Chlorquinol",
          "Halquinols",
          "Halquinol",
          "Chlofucid",
          "Clofuzid",
          "Endiaron",
          "Quesyl",
          "Quinolor",
          "CHQ",
          "Chloroxyquinoline",
          "Dichloroquinolinol",
          "Dichloroxin",
          "Dikhloroskin",
          "5,7-dichloroquinolin-8-ol",
          "Antiseborrheic Agents",
          "Although the mechanism of action is not understood, chloroxine may slow down mitotic activity in the epidermis, thereby reducing excessive scaling associated with dandruff or seborrheic dermatitis of the scalp. Chloroxine induces SOS-DNA repair in E. coli, so chloroxine may be genotoxic to bacteria."
       ]
    }
    '''

    void "test serialization to Compound Annotations"() {
        when:
        final CompoundAnnotations compoundAnnotations = objectMapper.readValue(COMPOUND_ANNOTATIONS, CompoundAnnotations.class)
        then:
        assert compoundAnnotations.getAnno_key().size() == compoundAnnotations.getAnno_val().size()
        Map<String, List<String>> annotations = compoundAnnotations.getAnnotations()
        assert annotations
        assert annotations.size() == 9

        assert annotations.get("COLLECTION").size() == 15
        assert annotations.get("COLLECTION") == compoundAnnotations.getCompoundCollection()

        assert annotations.get("Synonyms").size() == 22
        assert annotations.get("Synonyms") == compoundAnnotations.getSynonyms()

        assert annotations.get("DOCUMENTS").size() == 2
        assert annotations.get("DOCUMENTS") == compoundAnnotations.getDocuments()

        assert annotations.get("CompoundUNII").size() == 1
        assert annotations.get("CompoundUNII").get(0) == compoundAnnotations.getUniqueIngredientIdentifier()

        assert annotations.get("CompoundCAS").size() ==4
        annotations.get("CompoundCAS") == compoundAnnotations.getRegistryNumbers()

        assert annotations.get("CompoundIndication").size()==3
        annotations.get("CompoundIndication") == compoundAnnotations.getTherapeuticIndication()

        assert annotations.get("CompoundDrugLabelRx").size() ==1
        annotations.get("CompoundDrugLabelRx") == compoundAnnotations.getPrescriptionDrugLabel()

        assert annotations.get("CompoundMOA").size() == 2
        annotations.get("CompoundMOA") == compoundAnnotations.getMechanismOfAction()

        assert annotations.get("CompoundSpectra").size()==1
        annotations.get("CompoundSpectra") == compoundAnnotations.getCompoundSpectra()
    }


}

