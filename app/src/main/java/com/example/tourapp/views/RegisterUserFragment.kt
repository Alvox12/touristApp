package com.example.tourapp.views

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.example.tourapp.viewModel.RegisterUserViewModel
import com.example.tourapp.viewModel.RegisterViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_register.backToLogin
import kotlinx.android.synthetic.main.activity_register.btn_tags_select
import kotlinx.android.synthetic.main.fragment_register_user.*

class RegisterUserFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterUserFragment()
    }

    private lateinit var viewModel: RegisterViewModel

    private lateinit var user: User


    //Avisa de si se puede introducir los tags
    lateinit var observerTags: Observer<Boolean>
    lateinit var observerTagsDownloaded: Observer<Boolean>

    private lateinit var mListenerTags : ValueEventListener
    private var tagsDownloaded: MutableLiveData<Boolean> = MutableLiveData()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = (activity as RegisterActivity2).viewModel

        /*Al variar el valor de tagLiveData se ejecuta getTags*/
        observerTags = Observer {
            if(viewModel.tagLiveData.value!!) {
                getTags()
            }
        }

        /*tagLiveData ejeuta observerTags tras terminar de obtener datos usuario*/
        viewModel.tagLiveData.observe(viewLifecycleOwner, observerTags)

        observerTagsDownloaded = Observer {
            if(tagsDownloaded.value!!) {
                viewModel.arrayTags = (activity as RegisterActivity2).arrayListTags
                (activity as RegisterActivity2).showTagFragment()
            }
        }

        tagsDownloaded.observe(viewLifecycleOwner, observerTagsDownloaded)
    }

    override fun onStart() {
        super.onStart()

        btn_tags_select.setOnClickListener {
            (activity as RegisterActivity2).onRegisterClick()
        }

        backToLogin.setOnClickListener {
            (activity as RegisterActivity2).backToLogin()
        }
    }

    /**Descarga etiquetas o tags de ls base de datos*/
    fun getTags() {
        (activity as RegisterActivity2).arrayListTags.clear()
        val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)

        mListenerTags = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_TAGS", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("FIREBASE_BBDD_TAGS", "EXITO AL DESCARGAR INFO")

                snapshot.children.forEach {
                    //arrayListTags[it.key!!.toInt()] = it.value.toString()
                    (activity as RegisterActivity2).arrayListTags.add(it.value.toString())
                }

                tagsDownloaded.value = true
            }

        }

        tagsRef.addValueEventListener(mListenerTags)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.tagLiveData.removeObserver(observerTags)
        viewModel.tagLiveData.value = false

        tagsDownloaded.removeObserver(observerTagsDownloaded)
        tagsDownloaded.value = false
    }

}