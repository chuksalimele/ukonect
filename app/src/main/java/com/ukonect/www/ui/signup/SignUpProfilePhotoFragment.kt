package com.ukonect.www.ui.signup

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ukonect.www.R
import com.ukonect.www.Ukonect
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.AuthState
import com.ukonect.www.databinding.SignUpProfilePhotoFragmentBinding
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Constants.PPROFIE_PHOTO_REQUEST
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class SignUpProfilePhotoFragment : Fragment() {
    private var selectedImage: Uri? = null
    private var disposable: Disposable? = null
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }

    private var _binding: SignUpProfilePhotoFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SignUpProfilePhotoFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignUpProfilePhotoFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding?.signUpProfilePhotoPhotoBtn?.setOnClickListener {
            pickImage()
        }

        binding?.signUpProfilePhotoBtnNext?.setOnClickListener {

            var status_message = binding?.signUpProfilePhotoStatusMessageTxt?.text.toString()
            var photo_file_extension = ""

            disposable = Observable.fromCallable{
                //return the line below
                selectedImage?.toFile()?.let{
                    photo_file_extension = it.extension
                    Utils.getBase64StringFromFile(it)
                }

            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { photo_base64 ->

                        var req = mapOf<String, String>(
                            "user_id" to (appUser?.user_id ?: ""),
                            "photo_file_extension" to photo_file_extension,
                            "photo_base64" to (photo_base64 ?: ""),
                            "status_message" to status_message
                        )

                        authModel.singupStage(req, AuthState.AUTH_STAGE_PROFILE_PHOTO)
                    },
                    { error ->  Utils.handleException(context,  error) }
                )
        }

        binding?.signUpProfilePhotoBtnBack?.setOnClickListener {
            //TODO go back
        }
		
		val observer = Observer<AppUser> { app_user ->
		
			if(app_user.auth_state == AuthState.AUTH_STAGE_SUCCESS){

                view.visibility = View.VISIBLE //come back

                navController.navigate(R.id.move_to_homeFragment)
            }
		
		}
		
		// Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        authModel.auth.observe(viewLifecycleOwner, observer)

        super.onViewCreated(view, savedInstanceState)
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PPROFIE_PHOTO_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PPROFIE_PHOTO_REQUEST && resultCode === Activity.RESULT_OK && attr.data != null) {
            selectedImage = data?.data
            try {
                // Do whatever you want with this bitmap (image)
                //val bitmapImage = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImage)
                binding?.signUpProfilePhotoImgPhoto?.setImageURI(selectedImage)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
