package gr.cpaleop.announcement.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import gr.cpaleop.announcement.domain.usecases.GetAnnouncementUseCase
import gr.cpaleop.announcement.domain.usecases.ObserveDownloadNotifierUseCase
import gr.cpaleop.common.extensions.toSingleEvent
import gr.cpaleop.core.dispatchers.MainDispatcher
import gr.cpaleop.core.presentation.base.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class AnnouncementViewModel(
    @MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
    private val getAnnouncementUseCase: GetAnnouncementUseCase,
    private val announcementDetailsMapper: AnnouncementDetailsMapper,
    private val observeDownloadNotifierUseCase: ObserveDownloadNotifierUseCase
) : BaseViewModel() {

    private val _announcement = MutableLiveData<AnnouncementDetails>()
    val announcement: LiveData<AnnouncementDetails> = _announcement.toSingleEvent()

    val downloadStatus: MediatorLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(announcement) { announcementDetails ->
                viewModelScope.launch(mainDispatcher) {
                    observeDownloadNotifierUseCase(announcementDetails.id)
                        .flowOn(mainDispatcher)
                        .collect {
                            this@apply.value = it
                        }
                }
            }
        }
    }

    private val _attachmentFileId = MutableLiveData<AnnouncementDocument>()
    val attachmentFileId: LiveData<AnnouncementDocument> = _attachmentFileId.toSingleEvent()

    fun presentAnnouncement(id: String) {
        viewModelScope.launch(mainDispatcher) {
            try {
                _announcement.value = announcementDetailsMapper(getAnnouncementUseCase(id))
            } catch (t: Throwable) {
                Timber.e(t)
                handleNoConnectionException(t)
            }
        }
    }

    fun downloadAttachments(id: String) {
        viewModelScope.launch(mainDispatcher) {
            try {
                val mAnnouncement = getAnnouncementUseCase(id)
                _attachmentFileId.value =
                    AnnouncementDocument(mAnnouncement.id, mAnnouncement.attachments)
            } catch (t: Throwable) {
                Timber.e(t)
                handleNoConnectionException(t)
            }
        }
    }
}