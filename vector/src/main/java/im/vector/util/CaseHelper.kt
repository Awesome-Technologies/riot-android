/*
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.*

private const val LOG_TAG = "CaseHelper"

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    return formatter.format(date)
}

// Vitals

/**
 * Create json object for observation from the given data
 */
fun newBodyWeightObservation(data: Pair<Float, Date?>, patientName: String? = null): JsonObject {
    val weight = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "body-weight")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // ValueQuantity
    val valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "kg")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "kg")
    valueQuantity.addProperty("value", weight)
    json.add("valueQuantity", valueQuantity)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "29463-7")
    codingElement.addProperty("display", "Body Weight")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Body Weight")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newBodyTemperatureObservation(data: Pair<Float, Date?>, patientName: String? = null): JsonObject {
    val temperature = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "body-temperature")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // ValueQuantity
    val valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "Cel")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "C")
    valueQuantity.addProperty("value", temperature)
    json.add("valueQuantity", valueQuantity)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "8310-5")
    codingElement.addProperty("display", "Body temperature")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Body temperature")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newGlucoseObservation(data: Pair<Float, Date?>, patientName: String? = null): JsonObject {
    val glucose = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "glucose")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // ValueQuantity
    val valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "mg/dl")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "mg/dl")
    valueQuantity.addProperty("value", glucose)
    json.add("valueQuantity", valueQuantity)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "15074-8")
    codingElement.addProperty("display", "Glucose [Milligramm/volume] in Blood")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Glucose")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newOxygenObservation(data: Pair<Float, Date?>, patientName: String? = null): JsonObject {
    val saturation = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "oxygen")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // ValueQuantity
    val valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "%")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "%")
    valueQuantity.addProperty("value", saturation)
    json.add("valueQuantity", valueQuantity)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "59408-5")
    codingElement.addProperty("display", "Oxygen saturation in Arterial blood by Pulse oximetry")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Oxygen saturation")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newBloodPressureObservation(data: Triple<Float, Float, Date?>, patientName: String? = null): JsonObject {
    val systolic = data.first
    val diastolic = data.second
    val date = data.third

    val json = JsonObject()
    json.addProperty("id", "blood-pressure")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // Code
    var codingElement = JsonObject()
    codingElement.addProperty("code", "85354-9")
    codingElement.addProperty("display", "Blood pressure panel with all children optional")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    var code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Blood pressure systolic & diastolic")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    // Component
    val component = JsonArray()
    var componentElement = JsonObject()

    // Component -> Systolic
    codingElement = JsonObject()
    codingElement.addProperty("code", "8480-6")
    codingElement.addProperty("display", "Systolic blood pressure")
    codingElement.addProperty("system", "http://loinc.org")

    coding = JsonArray()
    coding.add(codingElement)

    code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Systolic blood pressure")
    componentElement.add("code", code)

    // Component -> Systolic -> ValueQuantity
    var valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "mm[Hg]")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "mmHg")
    valueQuantity.addProperty("value", systolic)
    componentElement.add("valueQuantity", valueQuantity)

    component.add(componentElement)
    componentElement = JsonObject()

    // Component -> Diastolic
    codingElement = JsonObject()
    codingElement.addProperty("code", "8462-4")
    codingElement.addProperty("display", "Diastolic blood pressure")
    codingElement.addProperty("system", "http://loinc.org")

    coding = JsonArray()
    coding.add(codingElement)

    code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Diastolic blood pressure")
    componentElement.add("code", code)

    // Component -> Diastolic -> ValueQuantity
    valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "mm[Hg]")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "mmHg")
    valueQuantity.addProperty("value", diastolic)
    componentElement.add("valueQuantity", valueQuantity)

    component.add(componentElement)

    json.add("component", component)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newPulseObservation(data: Pair<Float, Date?>, patientName: String? = null): JsonObject {
    val pulse = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "heart-rate")
    json.addProperty("resourceType", "Observation")

    // Meta
    val meta = JsonObject()
    meta.addProperty("profile", "http://hl7.org/fhir/StructureDefinition/vitalsigns")
    json.add("meta", meta)

    // ValueQuantity
    val valueQuantity = JsonObject()
    valueQuantity.addProperty("code", "/min")
    valueQuantity.addProperty("system", "http://unitsofmeasure.org")
    valueQuantity.addProperty("unit", "beats/minute")
    valueQuantity.addProperty("value", pulse)
    json.add("valueQuantity", valueQuantity)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "8867-4")
    codingElement.addProperty("display", "Heart rate")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Heart rate")
    json.add("code", code)

    // Category
    val categoryElement = JsonObject()
    categoryElement.addProperty("code", "vital-signs")
    categoryElement.addProperty("display", "Vital Signs")
    categoryElement.addProperty("system", "http://hl7.org/fhir/observation-category")

    coding = JsonArray()
    coding.add(codingElement)

    val category = JsonObject()
    category.add("coding", coding)
    category.addProperty("text", "Vital Signs")
    json.add("code", category)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

// Anamnesis

/**
 * Create json object for observation from the given data
 */
fun newResponsivenessObservation(data: Pair<String, Date?>, patientName: String? = null): JsonObject {
    val responsiveness = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "responsiveness")
    json.addProperty("resourceType", "Observation")

    // ValueString
    json.addProperty("valueString", responsiveness)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newPainObservation(data: Pair<String, Date?>, patientName: String? = null): JsonObject {
    val pain = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "pain")
    json.addProperty("resourceType", "Observation")

    // ValueString
    json.addProperty("valueString", pain)

    // Code
    val codingElement = JsonObject()
    codingElement.addProperty("code", "28319-2")
    codingElement.addProperty("display", "Pain status")
    codingElement.addProperty("system", "http://loinc.org")

    var coding = JsonArray()
    coding.add(codingElement)

    val code = JsonObject()
    code.add("coding", coding)
    code.addProperty("text", "Pain status")
    json.add("code", code)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newMiscObservation(data: Pair<String, Date?>, patientName: String? = null): JsonObject {
    val misc = data.first
    val date = data.second

    val json = JsonObject()
    json.addProperty("id", "misc")
    json.addProperty("resourceType", "Observation")

    // ValueString
    json.addProperty("valueString", misc)

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }
    date?.let {
        json.addProperty("effectiveDateTime", formatDate(it))
    }

    return json
}

/**
 * Create json object for observation from the given data
 */
fun newLastDefecationObservation(date: Date, patientName: String? = null): JsonObject {
    val json = JsonObject()
    json.addProperty("id", "last-defecation")
    json.addProperty("resourceType", "Observation")

    // Date
    json.addProperty("effectiveDateTime", formatDate(date))

    patientName?.let {
        json.addProperty("subject", "Patient/${it}")
    }

    return json
}