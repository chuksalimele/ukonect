package com.ukonect.www.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.User
import com.ukonect.www.data.AppUser
import com.ukonect.www.databinding.EditProfileFragmentBinding
import com.ukonect.www.remote.model.Model
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class EditProfileFragment : Fragment() {
    private var selectedImage: Uri? = null
    private var disposable: Disposable? = null
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }

    private var _binding: EditProfileFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EditProfileFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.editProfileChangeProfileBtn?.setOnClickListener {
            pickImage()
        }

        binding?.editProfileSaveBtn?.setOnClickListener(getOnClickSaveBtnListener())

        val layout = binding?.editProfileCollapsingToolbarLayout
        val toolbar = binding?.editProfileToolbar
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        if (toolbar != null) {
            layout?.setupWithNavController(toolbar, navController, appBarConfiguration)
        }

        var mainActivity = this.activity as MainActivity
        mainActivity.setSupportActionBar(toolbar)

        //Update the UI
        binding?.user = authModel.app_user; // definitely the app user
        binding?.appUser =authModel.app_user

        binding?.editProfileEditInfoInclude?.user = authModel.app_user

        var display_name = authModel.app_user?.display_name
        var space =" "
        (activity as MainActivity).supportActionBar?.setTitle(space)

        binding?.editProfileAppBar?.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset -> //  Vertical offset == 0 indicates appBar is fully  expanded.

            //If the AppBarLayout’s ‘verticalOffset’ is zero, then its fully expanded

            if (Math.abs(verticalOffset) > 200) {
                //appBarExpanded = false
                //invalidateOptionsMenu()
                (activity as MainActivity).supportActionBar?.setTitle(display_name)

            } else {
                (activity as MainActivity).supportActionBar?.setTitle(space)
                //appBarExpanded = true
                //invalidateOptionsMenu()
            }

        })

    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.PPROFIE_PHOTO_REQUEST)
    }

    private fun getOnClickSaveBtnListener(): View.OnClickListener? {
        var photo_file_extension = ""
        return View.OnClickListener { view ->

            disposable = Observable.fromCallable {
                //return the line below
                selectedImage?.toFile()?.let {
                    photo_file_extension = it.extension
                    Utils.getBase64StringFromFile(it)
                }

            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { photo_base64 ->
                        val c = binding?.editProfileEditInfoInclude;

                        val profileDetail = User()
                        profileDetail.user_id = authModel.app_user?.user_id ?: ""
                        profileDetail.photo_base64 = photo_base64 ?: ""
                        profileDetail.photo_file_extension = photo_file_extension
                        profileDetail.first_name = c?.userEditInfoFirstNameTxt?.text.toString()
                        profileDetail.last_name = c?.userEditInfoLastNameTxt?.text.toString()
                        profileDetail.personal_email =
                            c?.userEditInfoPersonalEmailTxt?.text.toString()
                        profileDetail.work_email = c?.userEditInfoWorkEmailTxt?.text.toString()
                        profileDetail.home_address = c?.userEditInfoHomeAddressTxt?.text.toString()
                        profileDetail.office_address =
                            c?.userEditInfoOfficeAddressTxt?.text.toString()
                        profileDetail.website = c?.userEditWebsiteTxt?.text.toString()
                        profileDetail.status_message =
                            c?.userEditInfoStatusMessageTxt?.text.toString()
                        profileDetail.work_phone_no = c?.userEditInfoWorkPhoneNoTxt?.text.toString()

                        var mainActivity = activity as MainActivity
                        mainActivity.disposable =
                            mainActivity.wampService.getCaller()?.updateProfile(profileDetail)
                                ?.subscribeOn(Schedulers.io())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribe(
                                    { app_user -> onEdithProfileSuccess(app_user) },
                                    { error -> Utils.handleException(context, error) }
                                )

                    },
                    { error -> Utils.handleException(context, error) }
                )

        }
    }

    private fun onEdithProfileSuccess(app_user: AppUser) {

        Utils.logExternal(context, "onEdithProfileSuccess 1")//TESTING!!!

        authModel.updateAppUser(app_user)

        Utils.logExternal(context, "onEdithProfileSuccess 2")//TESTING!!!


        val bundle = bundleOf(Constants.USER_ID to app_user.user_id)

        Utils.logExternal(context, "onEdithProfileSuccess 3")//TESTING!!!

        navController.navigate(R.id.move_to_PersonalProfileFragmentt, bundle)

        Utils.logExternal(context, "onEdithProfileSuccess 4")//TESTING!!!


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.edit_profille_app_bar, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === Constants.PPROFIE_PHOTO_REQUEST && resultCode === Activity.RESULT_OK && android.R.attr.data != null) {
            selectedImage = data?.data
            try {
                // Do whatever you want with this bitmap (image)
                //val bitmapImage = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImage)

                binding?.editProfilePhotoImg?.setImageURI(selectedImage)

                //Log.i("Image Path", selectedImage?.getPath())

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
