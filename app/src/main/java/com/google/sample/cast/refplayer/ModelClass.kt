package com.google.sample.cast.refplayer

import java.util.*

class ModelClass : ArrayList<ModelClassItem>()

data class ModelClassItem(
    val category_type: String,
    val circle: String,
    val episodes: List<Episode>,
    val first_episode: Any,
    val has_children: Boolean,
    val has_products: Boolean,
    val id: Int,
    val image_type: String,
    val name: String,
    val object_type: String,
    val parent: Int,
    val seasons: List<Season>,
    val series_url: String,
    val url: String
)

data class Episode(
    val admin_viewcount: Int,
    val age_group: String,
    val amounts: Amounts,
    val category: List<Int>,
    val category_type: String,
    val creator: Any,
    val description: String,
    val expiry_date: Any,
    val id: Int,
    val is_bookmarked: Boolean,
    val is_dolby: Boolean,
    val main_category: Int,
    val media: String,
    val meta_data: String,
    val movie_class: String,
    val object_type: String,
    val original_portrait_file: String,
    val original_thumbnail_file: String,
    val portrait: String,
    val portrait_url: String,
    val purchased: Boolean,
    val subscription: Boolean,
    val thumbnail: String,
    val thumbnail_url: String,
    val title: String,
    val trailer: Any,
    val trailer_file: String,
    val url: String,
    val year: Int
)

data class Season(
    val category_type: String,
    val children: List<Any>,
    val circle: Any,
    val episodes: List<Episode>,
    val first_episode: Any,
    val has_children: Boolean,
    val has_products: Boolean,
    val id: Int,
    val image_type: String,
    val name: String,
    val object_type: String,
    val parent: Int,
    val series_url: String,
    val url: String
)

data class Amounts(
    val AED: Any,
    val INR: Any,
    val USD: Any
)