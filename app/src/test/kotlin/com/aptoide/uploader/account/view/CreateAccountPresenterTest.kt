package com.aptoide.uploader.account.view

import com.aptoide.uploader.TestData
import com.aptoide.uploader.account.AccountPersistence
import com.aptoide.uploader.account.AptoideAccount
import com.aptoide.uploader.account.AptoideAccountManager
import com.aptoide.uploader.account.network.AccountResponse
import com.aptoide.uploader.account.network.AccountResponseMapper
import com.aptoide.uploader.account.network.RetrofitAccountService
import com.aptoide.uploader.network.ResponseV7
import com.aptoide.uploader.security.SecurityAlgorithms
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.Response


@RunWith(JUnitPlatform::class)
class CreateAccountPresenterTest : Spek({
    describe("a create account presenter") {
        it("should navigate to my apps when the user creates a new account in aptoide inserting valid data: email, password, store name and store privacy") {
            val navigator = mock<CreateAccountNavigator>()
            val serviceV2 = mock<RetrofitAccountService.ServiceV2>()
            val serviceV3 = mock<RetrofitAccountService.ServiceV3>()
            val serviceV7 = mock<RetrofitAccountService.ServiceV7>()
            val accountPersistence = mock<AccountPersistence>()
            val accountManager = AptoideAccountManager(RetrofitAccountService(serviceV2, serviceV3,
                    serviceV7, SecurityAlgorithms(), AccountResponseMapper()), accountPersistence)

            val view = mock<CreateAccountView>()
            val accountPresenter = CreateAccountPresenter(view, accountManager, navigator, CompositeDisposable(), Schedulers.trampoline())

            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
            val accounts = PublishSubject.create<AptoideAccount>()
            val createAccountEvent = PublishSubject.create<CreateAccountView.ViewModel>()
            val createAccountResponse = TestData.SUCCESS_RESPONSE
            val accountResponse = Response.success(AccountResponse(AccountResponse
                    .Nodes(AccountResponse.GetUserMeta(AccountResponse.GetUserMeta
                            .Data(AccountResponse.Store(TestData.STORE_NAME,
                                    "http://aptoide.com/avatar", 1)))),
                    ResponseV7.Info(ResponseV7.Info.Status.OK), null))

            whenever(accountPersistence.account).doReturn(accounts)
            whenever(accountPersistence.save(any())).doReturn(Completable.fromAction({
                accounts.onNext(AptoideAccount(true, true, TestData.STORE_NAME))
            }))
            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            whenever(view.createAccountEvent).doReturn(createAccountEvent)

            whenever(serviceV2.createAccount(any()))
                    .doReturn(createAccountResponse.toSingle().toObservable())
            whenever(serviceV7.getUserInfo(any())).doReturn(accountResponse
                    .toSingle().toObservable())

            accountPresenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
            createAccountEvent.onNext(CreateAccountView.ViewModel(
                    TestData.USER_NAME, TestData.USER_PASSWORD, TestData.STORE_NAME
            ))

            verify(view).showLoading()
            verify(view).hideLoading()
            verify(navigator).navigateToMyAppsView()
        }
    }
})