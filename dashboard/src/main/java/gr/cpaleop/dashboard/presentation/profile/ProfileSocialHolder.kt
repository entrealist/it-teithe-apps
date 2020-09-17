package gr.cpaleop.dashboard.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import gr.cpaleop.dashboard.databinding.ItemProfileSocialBinding

class ProfileSocialHolder(
    private val binding: ItemProfileSocialBinding,
    private val moreClickListener: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root), ProfileBindableHolder {

    override fun bind(item: ProfileSocialDetails) {
        binding.profileSocialMoreImageView.setOnClickListener { moreClickListener(item.label) }
        binding.profileSocialLogoImageView.setImageResource(item.socialLogoResource)
        binding.profileSocialContentTextView.text = item.content
    }

    companion object {

        fun create(parent: ViewGroup, moreClickListener: (String) -> Unit): ProfileSocialHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemProfileSocialBinding.inflate(inflater, parent, false)
            return ProfileSocialHolder(binding, moreClickListener)
        }
    }
}