package com.example.example.backstack

object Routes {
    const val Root = "root"

    const val FeedGraph = "feed_graph"
    const val SearchGraph = "search_graph"
    const val ProfileGraph = "profile_graph"

    const val Feed = "feed"
    const val FeedDetail = "feed/detail/{id}"

    const val Search = "search"
    const val Profile = "profile"

    fun feedDetail(id: Int) = "feed/detail/$id"
}