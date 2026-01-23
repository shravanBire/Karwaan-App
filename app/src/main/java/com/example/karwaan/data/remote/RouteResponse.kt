package com.example.karwaan.data.remote

data class RouteResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: Geometry,
    val distance: Double,
    val duration: Double
)

data class Geometry(
    val coordinates: List<List<Double>>
)

