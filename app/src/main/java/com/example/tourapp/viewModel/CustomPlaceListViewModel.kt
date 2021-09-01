package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class CustomPlaceListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerPlaceListAdapter
    lateinit var tagsAdapter: RecyclerTagListAdapter
    var listPlace: ArrayList<Place> = ArrayList()
    var keysPlaces: ArrayList<String> = arrayListOf()
    var listTags: ArrayList<String> = arrayListOf()
    var myBitmapIcon: MutableMap<Int, Bitmap> = mutableMapOf()
    private lateinit var mListenerPlace : ValueEventListener
    private lateinit var mListenerCustomList : ValueEventListener
    private lateinit var mListenerScores: ValueEventListener

    var initialDownload: Boolean = true

    lateinit var listCode: String

    lateinit var user: User
    lateinit var idCreator: String

    var placeIndex = 0
    var descargas = 0

    enum class FilterCategory {
        NONE, USERID, HIGHRATE, LOWRATE, USERPREFS
    }

    private var numScores: MutableMap<String, Int> = mutableMapOf()

    private class ScoreClass(var id: String = "",
                             var score: Double = 0.0,
                             var num: Int? = 0): Comparator<ScoreClass>, Comparable<ScoreClass> {

        override fun compare(a: ScoreClass, b: ScoreClass): Int {
            if (a != null && b != null) {
                return a.compareTo(b)
            }
            return 0
        }

        override fun compareTo(other: ScoreClass): Int {
            if(this.num!! > other.num!!)
                return 1
            else if(this.num!! < other.num!!)
                return -1
            else
                return 0
        }
    }

    private class PrefsClass(var place: Place, var common: Int): Comparator<PrefsClass>, Comparable<PrefsClass> {
        override fun compare(a: PrefsClass, b: PrefsClass): Int {
            if (a != null && b != null) {
                return a.compareTo(b)
            }
            return 0
        }

        override fun compareTo(other: PrefsClass): Int {
            if(this.common!! > other.common!!)
                return 1
            else if(this.common!! < other.common!!)
                return -1
            else
                return 0
        }
    }

    init {
        getNumScores()
    }

    fun configAdapter() {
        myAdapter = RecyclerPlaceListAdapter(true)
        myAdapter.setCustomPlaceModel(this)
    }

    fun setPlaceList() {
        myAdapter.setPlaceList(listPlace)
        myAdapter.notifyDataSetChanged()
    }

    private fun setFilteredPlaceList(listFiltered: ArrayList<Place>) {
        myAdapter.setPlaceList(listFiltered)
        myAdapter.notifyDataSetChanged()
    }

    fun filterPlaceList(listAux: ArrayList<Place>, position: Int) {
        if(position == 0) {
            setFilteredPlaceList(listAux)
        }
        else {
            val listFiltered: ArrayList<Place> = ArrayList()
            for(aux in listAux) {
                if(aux.arrayTags.contains(position)) {
                    listFiltered.add(aux)
                }
            }

            setFilteredPlaceList(listFiltered)
        }
    }

    fun filterByCategory(position: Int, category: CustomPlaceListViewModel.FilterCategory) {

        var listFiltered: ArrayList<Place> = arrayListOf()

        when(category) {
            CustomPlaceListViewModel.FilterCategory.NONE -> {
                filterPlaceList(listPlace, position)
            }
            CustomPlaceListViewModel.FilterCategory.USERID -> {
                for(aux in listPlace) {
                    if(aux.placeCreator == user.userId)
                        listFiltered.add(aux)
                }

                filterPlaceList(listFiltered, position)
            }
            CustomPlaceListViewModel.FilterCategory.HIGHRATE-> {
                listFiltered = filterByRate(PlaceListViewModel.FilterCategory.HIGHRATE)
                filterPlaceList(listFiltered, position)
            }
            CustomPlaceListViewModel.FilterCategory.LOWRATE-> {
                listFiltered = filterByRate(PlaceListViewModel.FilterCategory.LOWRATE)
                filterPlaceList(listFiltered, position)
            }
            CustomPlaceListViewModel.FilterCategory.USERPREFS-> {
                listFiltered = filterByPrefs()
                filterPlaceList(listFiltered, position)
            }
        }

    }

    private fun filterByRate(category: PlaceListViewModel.FilterCategory): ArrayList<Place> {
        var listFiltered: ArrayList<Place> = orderList()
        if(category == PlaceListViewModel.FilterCategory.LOWRATE) {
            listFiltered = listFiltered.reversed() as ArrayList<Place>
        }

        return listFiltered
    }

    private fun orderList(): ArrayList<Place> {
        val mapPlacesScores: MutableMap<String, Double> = mutableMapOf()
        for(place in listPlace.iterator()) {
            mapPlacesScores[place.placeId] = place.placeScore
        }

        val sortedScores = mapPlacesScores.toList().sortedBy {(_, score)-> score}.reversed().toMap()

        val aux1: MutableMap<Double, PriorityQueue<CustomPlaceListViewModel.ScoreClass>> = mutableMapOf()
        var currentScore: Double = sortedScores.values.first() //Le damos el valor mas alto
        var priorityQueue: PriorityQueue<CustomPlaceListViewModel.ScoreClass> = PriorityQueue<CustomPlaceListViewModel.ScoreClass>()
        for((id, score) in sortedScores) {
            if(numScores[id] == null)
                numScores[id] = 1

            if(score != currentScore) {
                if(priorityQueue.isNotEmpty()) {
                    aux1[currentScore] = priorityQueue
                    currentScore = score
                    priorityQueue = PriorityQueue<CustomPlaceListViewModel.ScoreClass>()
                }
            }

            priorityQueue.add(ScoreClass(id, score, numScores[id]))
        }
        aux1[currentScore] = priorityQueue

        val listFiltered: ArrayList<Place> = arrayListOf()
        for((score, pq) in aux1) {
            pq.forEach { scoreClass ->
                // Con scoreClass.id obtenemos el lugar y lo añadimos a listFiltered.
                val place = getPlaceById(scoreClass.id)
                if(place != null)
                    listFiltered.add(place)
            }
        }

        return listFiltered
    }

    private fun getPlaceById(id: String): Place? {
        listPlace.forEach { place ->
            if(place.placeId == id)
                return place
        }
        return null
    }

    private fun filterByPrefs(): ArrayList<Place> {
        val userPrefs = user.arrayPrefs
        var priorityQueue: PriorityQueue<CustomPlaceListViewModel.PrefsClass> = PriorityQueue<CustomPlaceListViewModel.PrefsClass>()
        listPlace.forEach { place ->
            val common = place.arrayTags.intersect(userPrefs)
            priorityQueue.add(CustomPlaceListViewModel.PrefsClass(place, common.size))
        }

        val listFiltered: ArrayList<Place> = arrayListOf()
        priorityQueue.forEach { prefClass ->
            listFiltered.add(prefClass.place)
        }

        return listFiltered
    }

    private fun getNumScores() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.PLACESCORES)
        numScores.clear()

        mListenerScores = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { idSnap->
                    if(idSnap.exists())
                        numScores[idSnap.key!!] = idSnap.childrenCount.toInt()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        ref.addValueEventListener(mListenerScores)
    }

    fun getTagsSelected(position: Int) {

    }

    fun getCustomListCodes() {
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${idCreator}/${Constants.USERLISTS}/${listCode}")
        mListenerCustomList = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                keysPlaces.clear()
                snapshot.children.forEach { code->
                   Log.v("FIREBASE_BBDD_USER", "KEY_OBTENIDA")
                    if(code.key != Constants.LISTNAME && code.key != Constants.LISTID)
                        keysPlaces.add(code.value as String)
                }

                if(initialDownload) {
                    initialDownload = false
                    getPlaceList()
                }
                else
                    setPlaceList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }
        }

        userRef.addValueEventListener(mListenerCustomList)
    }

    fun loadNewData() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        placeRef.removeEventListener(mListenerPlace)

        descargas = 0
        getPlaceList()
    }

    private fun getPlaceData(place: DataSnapshot): Place {

        var placeAux: Place

        val name = place.child(Constants.PLACENAME).value as String
        val description = place.child(Constants.PLACEDESCRIPTION).value as String
        val id = place.child(Constants.PLACEID).value as String
        val creator = place.child(Constants.PLACECREATOR).value as String
        val score = place.child(Constants.PLACESCORE).value
        val pictures = place.child(Constants.PLACEPICTURES).value as String
        val tags = place.child(Constants.PLACEETIQUETAS).value as String

        val coordinates = place.child(Constants.PLACECOORDINATES).value as String

        val arrayTags = getTags(tags)

        val doubleScore = score.toString()
        var scoreDouble = doubleScore.toDoubleOrNull()
        if (scoreDouble == null) {
            scoreDouble = 0.0
        }


        placeAux =  Place(id, name, description, creator, scoreDouble, pictures, coordinates, tags)
        placeAux.arrayTags = arrayTags

        return placeAux
    }


    fun getPlaceList() {

        //listPlace.clear()

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        var placeAux: Place

        mListenerPlace = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                listPlace.clear()

                while(placeIndex < keysPlaces.size && descargas < Constants.MAX_DATABASE_ITEMS) {

                    val key = keysPlaces[placeIndex]
                    if (snapshot.hasChild(key)) {

                        //Descargamos el lugar
                        placeAux = getPlaceData(snapshot.child(key))
                        listPlace.add(placeAux)

                        descargas++

                        setPlaceList()
                    }
                    placeIndex++
                }
            }

        }

        placeRef.addValueEventListener(mListenerPlace)
    }

    fun getTags(aux: String): ArrayList<Int> {

        val listInt: ArrayList<Int> = arrayListOf()

        if(aux != "") {
            val list: List<String> = aux.split(",")

            list.forEach { str ->
                listInt.add(Integer.parseInt(str))
            }
        }

        return listInt
    }


    fun deleteListElem(position: Int, placeId: String) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${idCreator}/${Constants.USERLISTS}/${listCode}")
        ref.child(placeId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                listPlace.removeAt(position)
                //keysPlaces.removeAt(position)
                Log.v("FIREBASE_BBDD_USER", "EXITO AL ELIMINAR ELEMENTO")
                setPlaceList()
            }
            else
                Log.v("FIREBASE_BBDD_USER", "ERROR AL ELIMINAR ELEMENTO")
        }
    }

    fun getLatLng(aux: String): LatLng {

        if(aux != "") {
            val list: List<String> = aux.split(",")
            val lat = list.first().toDoubleOrNull()
            val lng = list.last().toDoubleOrNull()

            if(lat != null && lng != null) {
                return LatLng(lat, lng)
            }
        }

        return LatLng(0.0, 0.0)
    }

    fun getFolderImages(folderDir: String) {
        if(folderDir != "") {

            val mStorage = FirebaseStorage.getInstance().reference
            val pictureRef = mStorage.child(folderDir)
            val maxDownloadBytes: Long = 1024 * 1024

            pictureRef.listAll().addOnSuccessListener { result ->
                for (fileRef in result.items) {
                    //Download the file using its reference (fileRef)
                    fileRef.getBytes(maxDownloadBytes).addOnSuccessListener { bytes ->
                        var bmp: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        //myBitmapIcon.put(index, bmp)
                    }
                }
            }

        }
    }

    private fun checkUserHasAccount(userId: String, placeId: String, place: DataSnapshot) {
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)

        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(Constants.USERID) && snapshot.exists()) {
                    //Descargamos el lugar
                }
                else {
                    //Eliminamos el lugar
                    placeRef.child(placeId).removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    fun deletePlaceListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${idCreator}/${Constants.USERLISTS}/${listCode}")
        placeRef.removeEventListener(mListenerPlace)
        userRef.removeEventListener(mListenerCustomList)

        val refScores = FirebaseDatabase.getInstance().getReference(Constants.PLACESCORES)
        refScores.removeEventListener(mListenerScores)
    }
}