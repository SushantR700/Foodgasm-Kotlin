package com.example.foodgasm.Map

data class DistanceMatrixResponse(val rows: List<Row>)
data class Row(val elements: List<Element>)
data class Element(val distance: Distance, val duration: Duration)
data class Distance(val text: String, val value: Int)
data class Duration(val text: String, val value: Int)
