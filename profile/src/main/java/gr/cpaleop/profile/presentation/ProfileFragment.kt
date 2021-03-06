package gr.cpaleop.profile.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.tabs.TabLayoutMediator
import gr.cpaleop.common.extensions.hideKeyboard
import gr.cpaleop.core.presentation.Message
import gr.cpaleop.profile.R
import gr.cpaleop.profile.databinding.FragmentProfileBinding
import gr.cpaleop.profile.di.profileModule
import gr.cpaleop.profile.presentation.options.OptionData
import gr.cpaleop.profile.presentation.options.SelectedSocialOption
import gr.cpaleop.profile.presentation.personal.PersonalOptionData
import gr.cpaleop.teithe_apps.presentation.base.BaseApiFragment
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import gr.cpaleop.teithe_apps.R as appR

class ProfileFragment :
    BaseApiFragment<FragmentProfileBinding, ProfileViewModel>(ProfileViewModel::class) {

    private var profileStateAdapter: ProfileStateAdapter? = null

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadKoinModules(profileModule)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        unloadKoinModules(profileModule)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), appR.color.colorBackgroundCard)
        binding.root.hideKeyboard()
        setupViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.presentProfile()
    }

    private fun setupViews() {
        profileStateAdapter = ProfileStateAdapter(this)
        binding.profileViewPager.run {
            adapter = profileStateAdapter
            offscreenPageLimit = 2
        }

        TabLayoutMediator(binding.profileTabLayout, binding.profileViewPager) { tab, position ->
            tab.setText(ProfileStateAdapter.titles[position])
        }.attach()
    }

    private fun observeViewModel() {
        viewModel.run {
            loading.observe(viewLifecycleOwner, Observer(::updateLoader))
            profile.observe(viewLifecycleOwner, Observer(::updateProfileDetails))
            choiceEditSocial.observe(viewLifecycleOwner, Observer(::editSocial))
            choiceEditPersonal.observe(viewLifecycleOwner, Observer(::editPersonal))
            choiceCopyToClipboard.observe(viewLifecycleOwner, Observer(::copyToClipboard))
        }
    }

    private fun updateProfileDetails(profilePresentation: ProfilePresentation) {
        binding.profileAmTextView.visibility = View.VISIBLE
        binding.profileUsernameTextView.visibility = View.VISIBLE
        binding.profileSemesterTextView.visibility = View.VISIBLE
        binding.profileRegisteredYearTextView.visibility = View.VISIBLE

        binding.profilePictureTextView.text = profilePresentation.initials
        binding.profileAmValueTextView.text = profilePresentation.am
        binding.profileUsernameValueTextView.text = profilePresentation.username
        binding.profileDisplayNameTextView.text = profilePresentation.displayName
        binding.profileSemesterValueTextView.text = profilePresentation.semester
        binding.profileRegisteredYearValueTextView.text = profilePresentation.registeredYear
    }

    private fun updateLoader(shouldLoad: Boolean) {
        binding.profilePictureProgressBar.isVisible = shouldLoad
    }

    private fun copyToClipboard(optionData: OptionData) {
        val clipboard =
            activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip =
            ClipData.newPlainText(requireContext().getString(optionData.titleRes), optionData.value)
        clipboard.setPrimaryClip(clip)
        showSnackbarMessage(Message(R.string.profile_toast_clipboard))
    }

    private fun editSocial(selectedSocialOption: SelectedSocialOption) {
        MaterialDialog(requireContext())
            .lifecycleOwner(viewLifecycleOwner)
            .title(
                R.string.profile_social_edit,
                requireContext().getString(selectedSocialOption.titleRes)
            )
            .cancelOnTouchOutside(true)
            .positiveButton(R.string.profile_social_edit_submit)
            .input(prefill = selectedSocialOption.value) { materialDialog, input ->
                viewModel.updateSocial(selectedSocialOption.social, input.toString())
                materialDialog.dismiss()
            }
            .negativeButton(R.string.profile_social_edit_cancel) {
                it.cancel()
            }
            .show()
    }

    private fun editPersonal(personalOptionData: PersonalOptionData) {
        MaterialDialog(requireContext())
            .lifecycleOwner(viewLifecycleOwner)
            .title(
                R.string.profile_social_edit,
                requireContext().getString(personalOptionData.labelRes)
            )
            .cancelOnTouchOutside(true)
            .positiveButton(R.string.profile_social_edit_submit)
            .input(prefill = personalOptionData.value) { materialDialog, input ->
                viewModel.updatePersonal(personalOptionData.type, input.toString())
                materialDialog.dismiss()
            }
            .negativeButton(R.string.profile_social_edit_cancel) {
                it.cancel()
            }
            .show()
    }
}