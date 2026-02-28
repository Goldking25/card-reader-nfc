package com.nfcpoc.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nfcpoc.data.model.ApduExchange;
import com.nfcpoc.data.model.CardType;
import com.nfcpoc.data.model.MifareSector;
import com.nfcpoc.data.model.NfcCard;
import com.nfcpoc.data.model.UltralightPage;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardDao_Impl implements CardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NfcCard> __insertionAdapterOfNfcCard;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<NfcCard> __deletionAdapterOfNfcCard;

  private final EntityDeletionOrUpdateAdapter<NfcCard> __updateAdapterOfNfcCard;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllCards;

  public CardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNfcCard = new EntityInsertionAdapter<NfcCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `nfc_cards` (`id`,`uid`,`uidRaw`,`cardType`,`label`,`timestamp`,`techList`,`sectors`,`mifareSize`,`mifareType`,`authenticatedSectorCount`,`pages`,`ultralightType`,`historicalBytes`,`atqa`,`sak`,`hiLayerResponse`,`applicationData`,`protocolInfo`,`idm`,`pmm`,`systemCode`,`apduLog`,`selectedAid`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NfcCard entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUid());
        statement.bindString(3, entity.getUidRaw());
        final String _tmp = __converters.fromCardType(entity.getCardType());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getLabel());
        statement.bindLong(6, entity.getTimestamp());
        final String _tmp_1 = __converters.fromStringList(entity.getTechList());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __converters.fromMifareSectors(entity.getSectors());
        statement.bindString(8, _tmp_2);
        statement.bindLong(9, entity.getMifareSize());
        statement.bindLong(10, entity.getMifareType());
        statement.bindLong(11, entity.getAuthenticatedSectorCount());
        final String _tmp_3 = __converters.fromUltralightPages(entity.getPages());
        statement.bindString(12, _tmp_3);
        statement.bindLong(13, entity.getUltralightType());
        if (entity.getHistoricalBytes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getHistoricalBytes());
        }
        if (entity.getAtqa() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getAtqa());
        }
        if (entity.getSak() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getSak());
        }
        if (entity.getHiLayerResponse() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getHiLayerResponse());
        }
        if (entity.getApplicationData() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getApplicationData());
        }
        if (entity.getProtocolInfo() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getProtocolInfo());
        }
        if (entity.getIdm() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getIdm());
        }
        if (entity.getPmm() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getPmm());
        }
        if (entity.getSystemCode() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getSystemCode());
        }
        final String _tmp_4 = __converters.fromApduLog(entity.getApduLog());
        statement.bindString(23, _tmp_4);
        if (entity.getSelectedAid() == null) {
          statement.bindNull(24);
        } else {
          statement.bindString(24, entity.getSelectedAid());
        }
        statement.bindString(25, entity.getNotes());
      }
    };
    this.__deletionAdapterOfNfcCard = new EntityDeletionOrUpdateAdapter<NfcCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `nfc_cards` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NfcCard entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfNfcCard = new EntityDeletionOrUpdateAdapter<NfcCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `nfc_cards` SET `id` = ?,`uid` = ?,`uidRaw` = ?,`cardType` = ?,`label` = ?,`timestamp` = ?,`techList` = ?,`sectors` = ?,`mifareSize` = ?,`mifareType` = ?,`authenticatedSectorCount` = ?,`pages` = ?,`ultralightType` = ?,`historicalBytes` = ?,`atqa` = ?,`sak` = ?,`hiLayerResponse` = ?,`applicationData` = ?,`protocolInfo` = ?,`idm` = ?,`pmm` = ?,`systemCode` = ?,`apduLog` = ?,`selectedAid` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NfcCard entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUid());
        statement.bindString(3, entity.getUidRaw());
        final String _tmp = __converters.fromCardType(entity.getCardType());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getLabel());
        statement.bindLong(6, entity.getTimestamp());
        final String _tmp_1 = __converters.fromStringList(entity.getTechList());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __converters.fromMifareSectors(entity.getSectors());
        statement.bindString(8, _tmp_2);
        statement.bindLong(9, entity.getMifareSize());
        statement.bindLong(10, entity.getMifareType());
        statement.bindLong(11, entity.getAuthenticatedSectorCount());
        final String _tmp_3 = __converters.fromUltralightPages(entity.getPages());
        statement.bindString(12, _tmp_3);
        statement.bindLong(13, entity.getUltralightType());
        if (entity.getHistoricalBytes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getHistoricalBytes());
        }
        if (entity.getAtqa() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getAtqa());
        }
        if (entity.getSak() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getSak());
        }
        if (entity.getHiLayerResponse() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getHiLayerResponse());
        }
        if (entity.getApplicationData() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getApplicationData());
        }
        if (entity.getProtocolInfo() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getProtocolInfo());
        }
        if (entity.getIdm() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getIdm());
        }
        if (entity.getPmm() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getPmm());
        }
        if (entity.getSystemCode() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getSystemCode());
        }
        final String _tmp_4 = __converters.fromApduLog(entity.getApduLog());
        statement.bindString(23, _tmp_4);
        if (entity.getSelectedAid() == null) {
          statement.bindNull(24);
        } else {
          statement.bindString(24, entity.getSelectedAid());
        }
        statement.bindString(25, entity.getNotes());
        statement.bindLong(26, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllCards = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM nfc_cards";
        return _query;
      }
    };
  }

  @Override
  public Object insertCard(final NfcCard card, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfNfcCard.insertAndReturnId(card);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCard(final NfcCard card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfNfcCard.handle(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCard(final NfcCard card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfNfcCard.handle(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllCards(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllCards.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllCards.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<NfcCard>> getAllCards() {
    final String _sql = "SELECT * FROM nfc_cards ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nfc_cards"}, new Callable<List<NfcCard>>() {
      @Override
      @NonNull
      public List<NfcCard> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
          final int _cursorIndexOfUidRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "uidRaw");
          final int _cursorIndexOfCardType = CursorUtil.getColumnIndexOrThrow(_cursor, "cardType");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTechList = CursorUtil.getColumnIndexOrThrow(_cursor, "techList");
          final int _cursorIndexOfSectors = CursorUtil.getColumnIndexOrThrow(_cursor, "sectors");
          final int _cursorIndexOfMifareSize = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareSize");
          final int _cursorIndexOfMifareType = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareType");
          final int _cursorIndexOfAuthenticatedSectorCount = CursorUtil.getColumnIndexOrThrow(_cursor, "authenticatedSectorCount");
          final int _cursorIndexOfPages = CursorUtil.getColumnIndexOrThrow(_cursor, "pages");
          final int _cursorIndexOfUltralightType = CursorUtil.getColumnIndexOrThrow(_cursor, "ultralightType");
          final int _cursorIndexOfHistoricalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "historicalBytes");
          final int _cursorIndexOfAtqa = CursorUtil.getColumnIndexOrThrow(_cursor, "atqa");
          final int _cursorIndexOfSak = CursorUtil.getColumnIndexOrThrow(_cursor, "sak");
          final int _cursorIndexOfHiLayerResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "hiLayerResponse");
          final int _cursorIndexOfApplicationData = CursorUtil.getColumnIndexOrThrow(_cursor, "applicationData");
          final int _cursorIndexOfProtocolInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "protocolInfo");
          final int _cursorIndexOfIdm = CursorUtil.getColumnIndexOrThrow(_cursor, "idm");
          final int _cursorIndexOfPmm = CursorUtil.getColumnIndexOrThrow(_cursor, "pmm");
          final int _cursorIndexOfSystemCode = CursorUtil.getColumnIndexOrThrow(_cursor, "systemCode");
          final int _cursorIndexOfApduLog = CursorUtil.getColumnIndexOrThrow(_cursor, "apduLog");
          final int _cursorIndexOfSelectedAid = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<NfcCard> _result = new ArrayList<NfcCard>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NfcCard _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUid;
            _tmpUid = _cursor.getString(_cursorIndexOfUid);
            final String _tmpUidRaw;
            _tmpUidRaw = _cursor.getString(_cursorIndexOfUidRaw);
            final CardType _tmpCardType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCardType);
            _tmpCardType = __converters.toCardType(_tmp);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final List<String> _tmpTechList;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTechList);
            _tmpTechList = __converters.toStringList(_tmp_1);
            final List<MifareSector> _tmpSectors;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfSectors);
            _tmpSectors = __converters.toMifareSectors(_tmp_2);
            final int _tmpMifareSize;
            _tmpMifareSize = _cursor.getInt(_cursorIndexOfMifareSize);
            final int _tmpMifareType;
            _tmpMifareType = _cursor.getInt(_cursorIndexOfMifareType);
            final int _tmpAuthenticatedSectorCount;
            _tmpAuthenticatedSectorCount = _cursor.getInt(_cursorIndexOfAuthenticatedSectorCount);
            final List<UltralightPage> _tmpPages;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfPages);
            _tmpPages = __converters.toUltralightPages(_tmp_3);
            final int _tmpUltralightType;
            _tmpUltralightType = _cursor.getInt(_cursorIndexOfUltralightType);
            final String _tmpHistoricalBytes;
            if (_cursor.isNull(_cursorIndexOfHistoricalBytes)) {
              _tmpHistoricalBytes = null;
            } else {
              _tmpHistoricalBytes = _cursor.getString(_cursorIndexOfHistoricalBytes);
            }
            final String _tmpAtqa;
            if (_cursor.isNull(_cursorIndexOfAtqa)) {
              _tmpAtqa = null;
            } else {
              _tmpAtqa = _cursor.getString(_cursorIndexOfAtqa);
            }
            final Integer _tmpSak;
            if (_cursor.isNull(_cursorIndexOfSak)) {
              _tmpSak = null;
            } else {
              _tmpSak = _cursor.getInt(_cursorIndexOfSak);
            }
            final String _tmpHiLayerResponse;
            if (_cursor.isNull(_cursorIndexOfHiLayerResponse)) {
              _tmpHiLayerResponse = null;
            } else {
              _tmpHiLayerResponse = _cursor.getString(_cursorIndexOfHiLayerResponse);
            }
            final String _tmpApplicationData;
            if (_cursor.isNull(_cursorIndexOfApplicationData)) {
              _tmpApplicationData = null;
            } else {
              _tmpApplicationData = _cursor.getString(_cursorIndexOfApplicationData);
            }
            final String _tmpProtocolInfo;
            if (_cursor.isNull(_cursorIndexOfProtocolInfo)) {
              _tmpProtocolInfo = null;
            } else {
              _tmpProtocolInfo = _cursor.getString(_cursorIndexOfProtocolInfo);
            }
            final String _tmpIdm;
            if (_cursor.isNull(_cursorIndexOfIdm)) {
              _tmpIdm = null;
            } else {
              _tmpIdm = _cursor.getString(_cursorIndexOfIdm);
            }
            final String _tmpPmm;
            if (_cursor.isNull(_cursorIndexOfPmm)) {
              _tmpPmm = null;
            } else {
              _tmpPmm = _cursor.getString(_cursorIndexOfPmm);
            }
            final String _tmpSystemCode;
            if (_cursor.isNull(_cursorIndexOfSystemCode)) {
              _tmpSystemCode = null;
            } else {
              _tmpSystemCode = _cursor.getString(_cursorIndexOfSystemCode);
            }
            final List<ApduExchange> _tmpApduLog;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfApduLog);
            _tmpApduLog = __converters.toApduLog(_tmp_4);
            final String _tmpSelectedAid;
            if (_cursor.isNull(_cursorIndexOfSelectedAid)) {
              _tmpSelectedAid = null;
            } else {
              _tmpSelectedAid = _cursor.getString(_cursorIndexOfSelectedAid);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new NfcCard(_tmpId,_tmpUid,_tmpUidRaw,_tmpCardType,_tmpLabel,_tmpTimestamp,_tmpTechList,_tmpSectors,_tmpMifareSize,_tmpMifareType,_tmpAuthenticatedSectorCount,_tmpPages,_tmpUltralightType,_tmpHistoricalBytes,_tmpAtqa,_tmpSak,_tmpHiLayerResponse,_tmpApplicationData,_tmpProtocolInfo,_tmpIdm,_tmpPmm,_tmpSystemCode,_tmpApduLog,_tmpSelectedAid,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCardById(final long id, final Continuation<? super NfcCard> $completion) {
    final String _sql = "SELECT * FROM nfc_cards WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<NfcCard>() {
      @Override
      @Nullable
      public NfcCard call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
          final int _cursorIndexOfUidRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "uidRaw");
          final int _cursorIndexOfCardType = CursorUtil.getColumnIndexOrThrow(_cursor, "cardType");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTechList = CursorUtil.getColumnIndexOrThrow(_cursor, "techList");
          final int _cursorIndexOfSectors = CursorUtil.getColumnIndexOrThrow(_cursor, "sectors");
          final int _cursorIndexOfMifareSize = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareSize");
          final int _cursorIndexOfMifareType = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareType");
          final int _cursorIndexOfAuthenticatedSectorCount = CursorUtil.getColumnIndexOrThrow(_cursor, "authenticatedSectorCount");
          final int _cursorIndexOfPages = CursorUtil.getColumnIndexOrThrow(_cursor, "pages");
          final int _cursorIndexOfUltralightType = CursorUtil.getColumnIndexOrThrow(_cursor, "ultralightType");
          final int _cursorIndexOfHistoricalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "historicalBytes");
          final int _cursorIndexOfAtqa = CursorUtil.getColumnIndexOrThrow(_cursor, "atqa");
          final int _cursorIndexOfSak = CursorUtil.getColumnIndexOrThrow(_cursor, "sak");
          final int _cursorIndexOfHiLayerResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "hiLayerResponse");
          final int _cursorIndexOfApplicationData = CursorUtil.getColumnIndexOrThrow(_cursor, "applicationData");
          final int _cursorIndexOfProtocolInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "protocolInfo");
          final int _cursorIndexOfIdm = CursorUtil.getColumnIndexOrThrow(_cursor, "idm");
          final int _cursorIndexOfPmm = CursorUtil.getColumnIndexOrThrow(_cursor, "pmm");
          final int _cursorIndexOfSystemCode = CursorUtil.getColumnIndexOrThrow(_cursor, "systemCode");
          final int _cursorIndexOfApduLog = CursorUtil.getColumnIndexOrThrow(_cursor, "apduLog");
          final int _cursorIndexOfSelectedAid = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final NfcCard _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUid;
            _tmpUid = _cursor.getString(_cursorIndexOfUid);
            final String _tmpUidRaw;
            _tmpUidRaw = _cursor.getString(_cursorIndexOfUidRaw);
            final CardType _tmpCardType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCardType);
            _tmpCardType = __converters.toCardType(_tmp);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final List<String> _tmpTechList;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTechList);
            _tmpTechList = __converters.toStringList(_tmp_1);
            final List<MifareSector> _tmpSectors;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfSectors);
            _tmpSectors = __converters.toMifareSectors(_tmp_2);
            final int _tmpMifareSize;
            _tmpMifareSize = _cursor.getInt(_cursorIndexOfMifareSize);
            final int _tmpMifareType;
            _tmpMifareType = _cursor.getInt(_cursorIndexOfMifareType);
            final int _tmpAuthenticatedSectorCount;
            _tmpAuthenticatedSectorCount = _cursor.getInt(_cursorIndexOfAuthenticatedSectorCount);
            final List<UltralightPage> _tmpPages;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfPages);
            _tmpPages = __converters.toUltralightPages(_tmp_3);
            final int _tmpUltralightType;
            _tmpUltralightType = _cursor.getInt(_cursorIndexOfUltralightType);
            final String _tmpHistoricalBytes;
            if (_cursor.isNull(_cursorIndexOfHistoricalBytes)) {
              _tmpHistoricalBytes = null;
            } else {
              _tmpHistoricalBytes = _cursor.getString(_cursorIndexOfHistoricalBytes);
            }
            final String _tmpAtqa;
            if (_cursor.isNull(_cursorIndexOfAtqa)) {
              _tmpAtqa = null;
            } else {
              _tmpAtqa = _cursor.getString(_cursorIndexOfAtqa);
            }
            final Integer _tmpSak;
            if (_cursor.isNull(_cursorIndexOfSak)) {
              _tmpSak = null;
            } else {
              _tmpSak = _cursor.getInt(_cursorIndexOfSak);
            }
            final String _tmpHiLayerResponse;
            if (_cursor.isNull(_cursorIndexOfHiLayerResponse)) {
              _tmpHiLayerResponse = null;
            } else {
              _tmpHiLayerResponse = _cursor.getString(_cursorIndexOfHiLayerResponse);
            }
            final String _tmpApplicationData;
            if (_cursor.isNull(_cursorIndexOfApplicationData)) {
              _tmpApplicationData = null;
            } else {
              _tmpApplicationData = _cursor.getString(_cursorIndexOfApplicationData);
            }
            final String _tmpProtocolInfo;
            if (_cursor.isNull(_cursorIndexOfProtocolInfo)) {
              _tmpProtocolInfo = null;
            } else {
              _tmpProtocolInfo = _cursor.getString(_cursorIndexOfProtocolInfo);
            }
            final String _tmpIdm;
            if (_cursor.isNull(_cursorIndexOfIdm)) {
              _tmpIdm = null;
            } else {
              _tmpIdm = _cursor.getString(_cursorIndexOfIdm);
            }
            final String _tmpPmm;
            if (_cursor.isNull(_cursorIndexOfPmm)) {
              _tmpPmm = null;
            } else {
              _tmpPmm = _cursor.getString(_cursorIndexOfPmm);
            }
            final String _tmpSystemCode;
            if (_cursor.isNull(_cursorIndexOfSystemCode)) {
              _tmpSystemCode = null;
            } else {
              _tmpSystemCode = _cursor.getString(_cursorIndexOfSystemCode);
            }
            final List<ApduExchange> _tmpApduLog;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfApduLog);
            _tmpApduLog = __converters.toApduLog(_tmp_4);
            final String _tmpSelectedAid;
            if (_cursor.isNull(_cursorIndexOfSelectedAid)) {
              _tmpSelectedAid = null;
            } else {
              _tmpSelectedAid = _cursor.getString(_cursorIndexOfSelectedAid);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _result = new NfcCard(_tmpId,_tmpUid,_tmpUidRaw,_tmpCardType,_tmpLabel,_tmpTimestamp,_tmpTechList,_tmpSectors,_tmpMifareSize,_tmpMifareType,_tmpAuthenticatedSectorCount,_tmpPages,_tmpUltralightType,_tmpHistoricalBytes,_tmpAtqa,_tmpSak,_tmpHiLayerResponse,_tmpApplicationData,_tmpProtocolInfo,_tmpIdm,_tmpPmm,_tmpSystemCode,_tmpApduLog,_tmpSelectedAid,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCardCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM nfc_cards";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<NfcCard>> searchCards(final String query) {
    final String _sql = "SELECT * FROM nfc_cards WHERE label LIKE '%' || ? || '%' ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nfc_cards"}, new Callable<List<NfcCard>>() {
      @Override
      @NonNull
      public List<NfcCard> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUid = CursorUtil.getColumnIndexOrThrow(_cursor, "uid");
          final int _cursorIndexOfUidRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "uidRaw");
          final int _cursorIndexOfCardType = CursorUtil.getColumnIndexOrThrow(_cursor, "cardType");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTechList = CursorUtil.getColumnIndexOrThrow(_cursor, "techList");
          final int _cursorIndexOfSectors = CursorUtil.getColumnIndexOrThrow(_cursor, "sectors");
          final int _cursorIndexOfMifareSize = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareSize");
          final int _cursorIndexOfMifareType = CursorUtil.getColumnIndexOrThrow(_cursor, "mifareType");
          final int _cursorIndexOfAuthenticatedSectorCount = CursorUtil.getColumnIndexOrThrow(_cursor, "authenticatedSectorCount");
          final int _cursorIndexOfPages = CursorUtil.getColumnIndexOrThrow(_cursor, "pages");
          final int _cursorIndexOfUltralightType = CursorUtil.getColumnIndexOrThrow(_cursor, "ultralightType");
          final int _cursorIndexOfHistoricalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "historicalBytes");
          final int _cursorIndexOfAtqa = CursorUtil.getColumnIndexOrThrow(_cursor, "atqa");
          final int _cursorIndexOfSak = CursorUtil.getColumnIndexOrThrow(_cursor, "sak");
          final int _cursorIndexOfHiLayerResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "hiLayerResponse");
          final int _cursorIndexOfApplicationData = CursorUtil.getColumnIndexOrThrow(_cursor, "applicationData");
          final int _cursorIndexOfProtocolInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "protocolInfo");
          final int _cursorIndexOfIdm = CursorUtil.getColumnIndexOrThrow(_cursor, "idm");
          final int _cursorIndexOfPmm = CursorUtil.getColumnIndexOrThrow(_cursor, "pmm");
          final int _cursorIndexOfSystemCode = CursorUtil.getColumnIndexOrThrow(_cursor, "systemCode");
          final int _cursorIndexOfApduLog = CursorUtil.getColumnIndexOrThrow(_cursor, "apduLog");
          final int _cursorIndexOfSelectedAid = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<NfcCard> _result = new ArrayList<NfcCard>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NfcCard _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUid;
            _tmpUid = _cursor.getString(_cursorIndexOfUid);
            final String _tmpUidRaw;
            _tmpUidRaw = _cursor.getString(_cursorIndexOfUidRaw);
            final CardType _tmpCardType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCardType);
            _tmpCardType = __converters.toCardType(_tmp);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final List<String> _tmpTechList;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTechList);
            _tmpTechList = __converters.toStringList(_tmp_1);
            final List<MifareSector> _tmpSectors;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfSectors);
            _tmpSectors = __converters.toMifareSectors(_tmp_2);
            final int _tmpMifareSize;
            _tmpMifareSize = _cursor.getInt(_cursorIndexOfMifareSize);
            final int _tmpMifareType;
            _tmpMifareType = _cursor.getInt(_cursorIndexOfMifareType);
            final int _tmpAuthenticatedSectorCount;
            _tmpAuthenticatedSectorCount = _cursor.getInt(_cursorIndexOfAuthenticatedSectorCount);
            final List<UltralightPage> _tmpPages;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfPages);
            _tmpPages = __converters.toUltralightPages(_tmp_3);
            final int _tmpUltralightType;
            _tmpUltralightType = _cursor.getInt(_cursorIndexOfUltralightType);
            final String _tmpHistoricalBytes;
            if (_cursor.isNull(_cursorIndexOfHistoricalBytes)) {
              _tmpHistoricalBytes = null;
            } else {
              _tmpHistoricalBytes = _cursor.getString(_cursorIndexOfHistoricalBytes);
            }
            final String _tmpAtqa;
            if (_cursor.isNull(_cursorIndexOfAtqa)) {
              _tmpAtqa = null;
            } else {
              _tmpAtqa = _cursor.getString(_cursorIndexOfAtqa);
            }
            final Integer _tmpSak;
            if (_cursor.isNull(_cursorIndexOfSak)) {
              _tmpSak = null;
            } else {
              _tmpSak = _cursor.getInt(_cursorIndexOfSak);
            }
            final String _tmpHiLayerResponse;
            if (_cursor.isNull(_cursorIndexOfHiLayerResponse)) {
              _tmpHiLayerResponse = null;
            } else {
              _tmpHiLayerResponse = _cursor.getString(_cursorIndexOfHiLayerResponse);
            }
            final String _tmpApplicationData;
            if (_cursor.isNull(_cursorIndexOfApplicationData)) {
              _tmpApplicationData = null;
            } else {
              _tmpApplicationData = _cursor.getString(_cursorIndexOfApplicationData);
            }
            final String _tmpProtocolInfo;
            if (_cursor.isNull(_cursorIndexOfProtocolInfo)) {
              _tmpProtocolInfo = null;
            } else {
              _tmpProtocolInfo = _cursor.getString(_cursorIndexOfProtocolInfo);
            }
            final String _tmpIdm;
            if (_cursor.isNull(_cursorIndexOfIdm)) {
              _tmpIdm = null;
            } else {
              _tmpIdm = _cursor.getString(_cursorIndexOfIdm);
            }
            final String _tmpPmm;
            if (_cursor.isNull(_cursorIndexOfPmm)) {
              _tmpPmm = null;
            } else {
              _tmpPmm = _cursor.getString(_cursorIndexOfPmm);
            }
            final String _tmpSystemCode;
            if (_cursor.isNull(_cursorIndexOfSystemCode)) {
              _tmpSystemCode = null;
            } else {
              _tmpSystemCode = _cursor.getString(_cursorIndexOfSystemCode);
            }
            final List<ApduExchange> _tmpApduLog;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfApduLog);
            _tmpApduLog = __converters.toApduLog(_tmp_4);
            final String _tmpSelectedAid;
            if (_cursor.isNull(_cursorIndexOfSelectedAid)) {
              _tmpSelectedAid = null;
            } else {
              _tmpSelectedAid = _cursor.getString(_cursorIndexOfSelectedAid);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new NfcCard(_tmpId,_tmpUid,_tmpUidRaw,_tmpCardType,_tmpLabel,_tmpTimestamp,_tmpTechList,_tmpSectors,_tmpMifareSize,_tmpMifareType,_tmpAuthenticatedSectorCount,_tmpPages,_tmpUltralightType,_tmpHistoricalBytes,_tmpAtqa,_tmpSak,_tmpHiLayerResponse,_tmpApplicationData,_tmpProtocolInfo,_tmpIdm,_tmpPmm,_tmpSystemCode,_tmpApduLog,_tmpSelectedAid,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
