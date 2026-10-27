package br.dev.guisleri.mototrack.ui.screen.profile.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.repository.AccountRepository

class ChangeEmailViewModelFactory(
    private val accountRepository: AccountRepository,
    private val currentEmail: String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ChangeEmailViewModel::class.java)) {
            return ChangeEmailViewModel(accountRepository, currentEmail) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
