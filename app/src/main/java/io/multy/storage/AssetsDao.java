/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.Objects;

import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.MultisigWallet;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.util.NativeDataHelper;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class AssetsDao {

    private Realm realm;

    public AssetsDao(@NonNull Realm realm) {
        this.realm = realm;
    }

    public void saveWallet(Wallet wallet) {
        realm.executeTransaction(realm -> saveSingleWallet(wallet));
    }

    public void saveWallets(List<Wallet> wallets) {
        realm.executeTransaction(realm -> {
            for (Wallet wallet : wallets) {

                Wallet toDelete = getWalletById(wallet.getId());
                if (toDelete != null) {
                    //todo there must being removing of all wallets and !inner objects
                    //realm is not deleting inner object. do it manually
                    if (toDelete.getAddresses() != null) {
                        for (WalletAddress walletAddress : toDelete.getAddresses()) {
                            if (walletAddress.getOutputs() != null) {
                                walletAddress.getOutputs().deleteAllFromRealm();
                            }
                        }

                        ((RealmList<WalletAddress>) toDelete.getAddresses()).deleteAllFromRealm();
                    }

                    if (toDelete.getBtcWallet() != null) {
                        toDelete.getBtcWallet().deleteFromRealm();
                    }
                    if (toDelete.getEthWallet() != null) {
                        toDelete.getEthWallet().deleteFromRealm();
                    }
                    if (toDelete.getMultisigWallet() != null) {
                        if (toDelete.getMultisigWallet().getOwners() != null) {
                            toDelete.getMultisigWallet().getOwners().deleteAllFromRealm();
                        }
                        toDelete.getMultisigWallet().deleteFromRealm();
                    }
                    toDelete.deleteFromRealm();
                }

                saveSingleWallet(wallet);
            }
            RealmResults<Wallet> realmWallets = getWallets();
            if (wallets.size() != realmWallets.size()) {
                for (Wallet realmWallet : realmWallets) {
                    if (!isWalletExist(realmWallet, wallets)) {
                        realmWallet.deleteFromRealm();
                    }
                }
            }
        });
    }

    private boolean isWalletExist(Wallet wallet, List<Wallet> wallets) {
        for (Wallet listWallet : wallets) {
            if (wallet.getId() == listWallet.getId()) {
                return true;
            }
        }
        return false;
    }

    private void saveSingleWallet(Wallet wallet) {
        final int index = wallet.getIndex();
        final String name = wallet.getWalletName();
        final String balance = wallet.getBalance();

        Wallet savedWallet = new Wallet();
        savedWallet.setDateOfCreation(wallet.getDateOfCreation());
        savedWallet.setLastActionTime(wallet.getLastActionTime());
        savedWallet.setIndex(index);
        savedWallet.setWalletName(name);
        savedWallet.setBalance(balance);
        savedWallet.setNetworkId(wallet.getNetworkId());
        savedWallet.setCurrencyId(wallet.getCurrencyId());
        savedWallet.setPending(wallet.isPending());
        savedWallet.setSyncing(wallet.isSyncing());

        if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
            savedWallet.setBtcWallet(wallet.getBtcWallet().asRealmObject(realm));
            savedWallet.setBalance(String.valueOf(savedWallet.getBtcWallet().calculateBalance()));
            savedWallet.setAvailableBalance(String.valueOf(savedWallet.getBtcWallet().calculateAvailableBalance()));

        } else if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.EOS.getValue()) {
            savedWallet.setEosWallet(wallet.getEosWallet().asRealmObject(realm));
            String eosBalance = savedWallet.getEosWallet().getBalance();
            if (!eosBalance.equals("0")) {
                eosBalance = savedWallet.getEosWallet().getDividedBalance(balance);
            }
            savedWallet.getEosWallet().setBalance(eosBalance);
            savedWallet.getEosWallet().setPendingBalance(eosBalance);
            savedWallet.setBalance(eosBalance);
            savedWallet.setAvailableBalance(eosBalance);

        } else {
            if (wallet.getMultisigWallet() != null) {
                savedWallet.setMultisigWallet(Objects.requireNonNull(wallet.getMultisigWallet()).asRealmObject(realm));
            }

            savedWallet.setEthWallet(Objects.requireNonNull(wallet.getEthWallet()).asRealmObject(realm));
            final String ethBalance = savedWallet.getEthWallet().getBalance();
            final String ethPendingBalance = savedWallet.getEthWallet().getPendingBalance();
            final String ethAvailableBalance = savedWallet.getEthWallet().calculateAvailableBalance(ethBalance);

            savedWallet.setBalance(ethBalance);
            savedWallet.setAvailableBalance(ethAvailableBalance);
        }

        realm.insertOrUpdate(savedWallet);
    }

    public RealmResults<Wallet> getWallets() {
        return realm.where(Wallet.class).sort("lastActionTime", Sort.DESCENDING).findAll();
    }

    public RealmResults<Wallet> getAvailableWallets() {
        return realm.where(Wallet.class)
                .notEqualTo("availableBalance", "0")
                .sort("lastActionTime", Sort.ASCENDING)
                .findAll();
    }

    public RealmResults<Wallet> getAvailableWallets(int currencyId, int networkId) {
        return realm.where(Wallet.class)
                .equalTo("networkId", networkId)
                .equalTo("currencyId", currencyId)
                .notEqualTo("availableBalance", "0")
                .sort("lastActionTime", Sort.ASCENDING)
                .findAll();
    }

    public RealmResults<Wallet> getAvailableBtcWallets() {
        return realm.where(Wallet.class).equalTo("currencyId", NativeDataHelper.Blockchain.BTC.getValue()).notEqualTo("availableBalance", "0").sort("lastActionTime", Sort.ASCENDING).findAll();
    }

    public RealmResults<Wallet> getWallets(int blockChainId, boolean includeMultisig) {
        RealmQuery<Wallet> query = realm.where(Wallet.class).equalTo("currencyId", blockChainId);
        return includeMultisig ? query.findAll() : query.isNull("multisigWallet").findAll();
    }

    public RealmResults<Wallet> getWallets(int blockChainId, int networkId, boolean includeMultisig) {
        RealmQuery<Wallet> query = realm.where(Wallet.class).equalTo("currencyId", blockChainId)
                .equalTo("networkId", networkId);
        return includeMultisig ? query.findAll() : query.isNull("multisigWallet").findAll();
    }

    public Wallet getWallet(int blockChainId, int networkId, int walletIndex) {
        return realm.where(Wallet.class).equalTo("currencyId", blockChainId)
                .equalTo("networkId", networkId).equalTo("index", walletIndex)
                .isNull("multisigWallet").findFirst();
    }

    @Nullable
    public Wallet getMultisigLinkedWallet(List<Owner> owners) {
        for (Owner owner : owners) {
            if (!TextUtils.isEmpty(owner.getUserId())) {
                return getMultisigLinkedWallet(owner.getAddress());
            }
        }
        return null;
    }

    @Nullable
    public Wallet getMultisigLinkedWallet(String linkedAddress) {
        return realm.where(Wallet.class).isNull("multisigWallet").equalTo("ethWallet.addresses.address", linkedAddress).findFirst();
    }

    public Wallet getMultisigWallet(int blockChainId, int networkId, int walletIndex) {
        return realm.where(Wallet.class).equalTo("currencyId", blockChainId)
                .equalTo("networkId", networkId).equalTo("index", walletIndex)
                .isNotNull("multisigWallet").findFirst();
    }

    public Wallet getMultisigWallet(String inviteCode) {
        return realm.where(Wallet.class).isNotNull("multisigWallet")
                .equalTo("multisigWallet.inviteCode", inviteCode).findFirst();
    }

    public RealmResults<Wallet> getMultisigWallets() {
        return realm.where(Wallet.class).isNotNull("multisigWallet").findAll();
    }

    public boolean isSelfOwnerAddress(String address) {
        return realm.where(Owner.class).isNotNull("userId").isNotEmpty("userId")
                .equalTo("address", address).findFirst() != null;
    }

    public void saveBtcAddress(long id, WalletAddress address) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(id);
            wallet.getBtcWallet().getAddresses().add(realm.copyToRealm(address));
            realm.insertOrUpdate(wallet);
        });
    }

    public void delete(@NonNull final RealmObject object) {
        realm.executeTransaction(realm -> object.deleteFromRealm());
    }

    public void deleteAll() {
        realm.executeTransaction(realm -> {
            realm.where(Wallet.class).findAll().deleteAllFromRealm();
            realm.where(BtcWallet.class).findAll().deleteAllFromRealm();
            realm.where(EthWallet.class).findAll().deleteAllFromRealm();
            realm.where(WalletAddress.class).findAll().deleteAllFromRealm();
            realm.where(MultisigWallet.class).findAll().deleteAllFromRealm();
        });
    }

    public Wallet getWalletById(long id) {
        return realm.where(Wallet.class).equalTo("dateOfCreation", id).findFirst();
    }

    public void updateWalletName(long id, String newName) {
        realm.executeTransaction(realm1 -> {
            Wallet wallet = getWalletById(id);
            wallet.setWalletName(newName);
            realm1.insertOrUpdate(wallet);
        });
    }

    public void removeWallet(long id) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(id);
            wallet.deleteFromRealm();
        });
    }

    public RealmResults<WalletAddress> getWalletAddress(String address) {
        return realm.where(WalletAddress.class).equalTo("address", address).findAll();
    }

    public void saveRecentAddress(RecentAddress recentAddress) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(recentAddress));
    }

    public RealmResults<RecentAddress> getRecentAddresses() {
        return realm.where(RecentAddress.class).findAll();
    }

    public RealmResults<RecentAddress> getRecentAddresses(int currencyId, int networkId) {
        return realm.where(RecentAddress.class).equalTo(RecentAddress.CURRENCY_ID, currencyId)
                .equalTo(RecentAddress.NETWORK_ID, networkId).findAll();
    }

    public boolean ifAddressExist(long addressTo) {
        RealmQuery<RecentAddress> query = realm.where(RecentAddress.class).equalTo(RecentAddress.RECENT_ADDRESS_ID, addressTo);
        return query.count() != 0;
    }
}
