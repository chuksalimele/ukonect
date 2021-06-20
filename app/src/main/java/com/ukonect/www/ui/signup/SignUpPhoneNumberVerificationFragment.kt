package com.ukonect.www.ui.signup

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.AuthState
import com.ukonect.www.databinding.SignUpPhoneNumberVerificationFragmentBinding
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Utils


class SignUpPhoneNumberVerificationFragment : Fragment() {
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }

    private var _binding: SignUpPhoneNumberVerificationFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SignUpPhoneNumberVerificationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignUpPhoneNumberVerificationFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var selected_country = ""
		var ccp = binding?.signUpPhoneNumberVerificationSpnCountry

        binding?.signUpPhoneNumberVerificationLblZipCode?.text?.append("${ccp?.selectedCountryCodeWithPlus}")

        ccp?.setOnCountryChangeListener {
            binding?.signUpPhoneNumberVerificationLblZipCode?.text?.clear()
            binding?.signUpPhoneNumberVerificationLblZipCode?.text?.append("${ccp.selectedCountryCodeWithPlus}")
        }

        binding?.signUpPhoneNumberVerificationBtnNext?.setOnClickListener {


            var phone_no = binding?.signUpPhoneNumberVerificationTxtPhoneNo?.text.toString()
            var country = ccp?.selectedCountryName
            var country_code = ccp?.selectedCountryNameCode
            var dialling_code = "${ccp?.selectedCountryCodeAsInt}"
            var phoneNo =
                country_code?.let { it1 -> Utils.reformPhoneNumber(requireContext(), phone_no, it1) }

            /*AlertDialog.Builder(this.requireContext())//TODO TO BE REMOVED ABEG O!!!
                .setTitle("Something")
                .setMessage("country_code: ${Utils.countryNameCode(requireContext())}," +
                        " dialling_code: ${Utils.countryNumericCode(requireContext(), Utils.countryNameCode(requireContext()))}," +
                        " nomarlize phone number:  ${Utils.reformPhoneNumber(
                            requireContext(),
                            phone_no,
                            Utils.countryNameCode(requireContext())
                        )}")
                .create()
                .show()*/

             // display the dialling code

           var user_id = phoneNo?.numberE164?:""

            var req = mapOf<String, String>(
                "user_id" to user_id,
                "mobile_phone_no" to (phoneNo?.nationNumber?:""),
                "dialling_code" to dialling_code,
                "country_code" to (country_code?:""),
                "country" to (country?:""))

            authModel.singupStage(req, AuthState.AUTH_STAGE_PHONE_NUMBER_VERIFY)
        }

        binding?.signUpPhoneNumberVerificationBtnBack?.setOnClickListener {
            //TODO go back
        }
		
		val observer = Observer<AppUser> { app_user ->
		
			if(app_user.auth_state == AuthState.AUTH_STAGE_CONFIRM_VERIFICATION_CODE){

                val direction =
                            SignUpPhoneNumberVerificationFragmentDirections.moveToSignUpConfirmVerificationCodeFragment()

				navController.navigate(direction)
            }
		
		}
		
		// Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        authModel.auth.observe(viewLifecycleOwner, observer)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}
