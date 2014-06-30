create or replace PROCEDURE CREATE_AUTOINCREMENT 
(
  TABLE_NAME IN VARCHAR2 -- имя таблицы, для которой создается автоинкремент
, ID_COL_NAME IN VARCHAR2 -- имя столбца, для которого создается автоинкремент
, trg_head IN VARCHAR2 -- префикс, который будет использоваться для имени тригера и последовательсности
, additional_acts VARCHAR2 default null
) AS 
BEGIN
  BEGIN
    EXECUTE Immediate 'DROP SEQUENCE '||trg_head||'_SEQ ';--'DROP SEQUENCE ' || trg_head || '_SEQ ';
  EXCEPTION
  WHEN OTHERS THEN
    NULL;
  END;  
   EXECUTE Immediate 'create SEQUENCE '||trg_head||'_SEQ INCREMENT BY 1 START WITH 1';

 
EXECUTE Immediate   '
create or replace trigger ' || trg_head ||'_id_trg 
before insert or update on ' || TABLE_NAME || '  
for each row 
begin  
  if inserting then 
      :new.' || ID_COL_NAME || ' := '||trg_head||'_SEQ.nextval;
  elsif updating then 
    if  :new.' || ID_COL_NAME || ' <>  :old.' || ID_COL_NAME || ' then 
       raise_application_error (-20555, ''Не могу помять значение первичного ключа''); 
     end if;
  end if;
  ' ||
  additional_acts || '
end;' ;
END CREATE_AUTOINCREMENT;
/

CREATE TABLE КОМПЛЕКТУЮЩИЕ 
(   КОМПЛ_ID NUMBER NOT NULL 
  , ГРУППА VARCHAR2(100)  
  , Наименование VARCHAR2(200) 
  , ТУ VARCHAR2(100) 
  , МАРКА VARCHAR2(100) 
  , ЕД_ИЗМЕРЕНИЯ VARCHAR2(40) default 'шт'
  , НА_СКЛАДЕ NUMBER(7,3) DEFAULT 0
  , КОМП_ИНФО varchar2(400)
  , ДАТА_КОМП DATE DEFAULT SYSDATE
  , ЦЕНА NUMBER(10,2) 
  , CONSTRAINT КОМПЛ_ПК PRIMARY KEY ( КОМПЛ_ID ) ENABLE 
  , CONSTRAINT КОМПЛ_УНИК_ИМЯ UNIQUE ( ТУ, МАРКА) ENABLE
  , CONSTRAINT "КОМПЛ_КВО>0" CHECK  (НА_СКЛАДЕ >= 0) ENABLE
);

begin
CREATE_AUTOINCREMENT('КОМПЛЕКТУЮЩИЕ', 'КОМПЛ_ID', 'КОМПЛ' , 
':new.МАРКА := upper(trim(:new.МАРКА)); 
  :new.ТУ := upper(trim(:new.ТУ));'
);
end;
/

drop TABLE СПЕЦИФИКАЦИЯ; 
CREATE TABLE СПЕЦИФИКАЦИЯ 
(
  СПЕЦ_ID NUMBER NOT NULL 
, ИМЯ_ФАЙЛА VARCHAR2(40)
, НОМ_ЗАКАЗА VARCHAR2(40)
, ПЛАН_З VARCHAR2(40)
, ИЗДЕЛИЕ VARCHAR2(200)
, БЛОК VARCHAR2(200)
, НОМ_БЛОКА VARCHAR2(40)
, НОМ_СТР_В_СПЕЦ NUMBER(4)
, СПЕЦ_ИНФО VARCHAR2(400)
, КОМПЛ_ID number not null CONSTRAINT Спец_ВК_КОМПЛid
                REFERENCES КОМПЛЕКТУЮЩИЕ(Компл_ID) ENABLE
, К_ВО_ПО_СПЕЦ NUMBER(7,3)  DEFAULT 1 CONSTRAINT К_ВО_NOT_NULL   NOT NULL
, ДАТА_СПЕЦ DATE DEFAULT sysdate 
, CONSTRAINT Спец_ПК PRIMARY KEY ( СПЕЦ_ID) ENABLE 
,CONSTRAINT СПЕЦ_УНИК_СТР UNIQUE (ИМЯ_ФАЙЛА,  НОМ_СТР_В_СПЕЦ) ENABLE
);

begin
 CREATE_AUTOINCREMENT('СПЕЦИФИКАЦИЯ', 'СПЕЦ_ID', 'СПЦ',
'if (:new.НОМ_СТР_В_СПЕЦ is null) then   
         :new.НОМ_СТР_В_СПЕЦ := -:new.СПЕЦ_ID;
      end if;'
);
end;
/

DROP TABLE ЗАЯВКИ;
CREATE TABLE ЗАЯВКИ 
(
  НОМ_ЗАЯВКИ VARCHAR2(40) NOT NULL 
, ДАТА_ЗАЯВКИ DATE DEFAULT SYSDATE 
, НОМ_СЧЕТА VARCHAR2(40) 
, ДТ_СЧЕТА DATE 
, ПОСТАВЩИК VARCHAR2(200) 
, Сумма_СЧТ NUMBER(9,2)
, Статус VARCHAR2(40) default 'СОСТАВЛЕН'  NOT NULL check ( Статус IN ('СОСТАВЛЕН','ЗАПРОШЕН','ПОДТВЕРЖДЕН','ОПЛАЧЕН', 'ЗАКРЫТ' )) 
, ДТ_ОПЛАТЫ DATE 
, СлужЗ VARCHAR2(40)
, Система VARCHAR2(200)
, ИЗДЕЛИЕ VARCHAR2(200)
, НОМ_ЗАКАЗА VARCHAR2(40)
, ЗАЯ_ИНФО VARCHAR2(1000)
, CONSTRAINT СЧЕТА_ПК PRIMARY KEY (НОМ_ЗАЯВКИ)  ENABLE 
, ЗАПР_НА_ИЗМ  VARCHAR2(16) check ( ЗАПР_НА_ИЗМ IN ( 'ЗАКРЫТО' ))
);

 
DROP TABLE ТРЕБОВАНИЯ; 
CREATE TABLE ТРЕБОВАНИЯ 
( 
   ТРЕБ_ID NUMBER NOT NULL 
  ,НОМ_ТРЕБ VARCHAR2(40) NOT NULL
  ,Спец_ID NUMBER not null CONSTRAINT ТРЕБ_ВК_СПЕЦ
                REFERENCES СПЕЦИФИКАЦИЯ(Спец_ID)   ENABLE
  ,НОМ_ЗАЯВКИ VARCHAR2(40)  CONSTRAINT Н_ЗАЯ_ВК_ЗАЯ
             REFERENCES ЗАЯВКИ(НОМ_ЗАЯВКИ) ON DELETE SET NULL ENABLE
  ,ЗАПРОШЕНО NUMBER(7,3) 
  ,ВЫДАНО NUMBER(7,3) 
  ,ID_ЗАПРОШ NUMBER not null CONSTRAINT ТРБ_ВК_К_ID
              REFERENCES КОМПЛЕКТУЮЩИЕ(КОМПЛ_ID)  ENABLE
  ,ID_ВЫДАН NUMBER not null CONSTRAINT ТРБ_ВК_К_ID1
              REFERENCES КОМПЛЕКТУЮЩИЕ(КОМПЛ_ID)  ENABLE
  ,ДАТА_ТРЕБ DATE DEFAULT SYSDATE
  ,ДАТА_ВЫДАЧИ DATE
  ,ТРЕБ_ИНФО VARCHAR2(1000)
  ,CONSTRAINT ТРЕБОВАНИЯ_ПК PRIMARY KEY 
   (ТРЕБ_ID)  ENABLE 
);

begin
 CREATE_AUTOINCREMENT('Требования', 'ТРЕБ_ID', 'ТРЕБ');
end;
/

DROP table детал_зая;
create table детал_зая (
  детал_зая_ID NUMBER(6)
,НОМ_ЗАЯВКИ VARCHAR(40) CONSTRAINT Д_З_ВК_З_ID
              REFERENCES ЗАЯВКИ(НОМ_ЗАЯВКИ)  ENABLE
,Компл_id CONSTRAINT Д_З_ВК_К_ID
              REFERENCES КОМПЛЕКТУЮЩИЕ(компл_id)  ENABLE
,Зая_Кол_во NUMBER(7,3)
,Стоимость_поз NUMBER(9,2)
);
execute CREATE_AUTOINCREMENT('ДЕТАЛ_ЗАЯ', 'ДЕТАЛ_ЗАЯ_ID', 'ЗАЯ');

DROP TABLE НАКЛАДНЫЕ;
CREATE TABLE НАКЛАДНЫЕ 
(
  НАКЛ_ID NUMBER NOT NULL PRIMARY KEY 
, НОМ_ЗАЯВКИ VARCHAR2(40) NOT NULL CONSTRAINT НАКЛ_ВК_Н_З
               REFERENCES Заявки(НОМ_ЗАЯВКИ) ENABLE
, Ном_ТОВ_НАКЛ VARCHAR2(100) 
, ДТ_ТОВ_НАКЛ DATE DEFAULT SYSDATE
, Ном_СФ VARCHAR2(100)
);

execute CREATE_AUTOINCREMENT('НАКЛАДНЫЕ', 'НАКЛ_ID', 'НАКЛ');


DROP TABLE ДЕТАЛ_НАКЛАДНЫХ;
CREATE TABLE ДЕТАЛ_НАКЛАДНЫХ 
(
  Д_НАКЛ_ID NUMBER NOT NULL PRIMARY KEY
  ,НАКЛ_ID NUMBER NOT NULL CONSTRAINT Д_НАКЛ_ВК_Н_ID
               REFERENCES НАКЛАДНЫЕ(НАКЛ_ID) ON DELETE CASCADE  ENABLE
 , КОМПЛ_ID NUMBER NOT NULL CONSTRAINT Д_НАКЛ_ВК_К_ID
               REFERENCES КОМПЛЕКТУЮЩИЕ(КОМПЛ_ID)  ENABLE
 , КОЛ_ВО NUMBER(7,3)  
);

execute CREATE_AUTOINCREMENT('ДЕТАЛ_НАКЛАДНЫХ', 'Д_НАКЛ_ID', 'Д_НАКЛ');


CREATE OR REPLACE VIEW ДЕФИЦИТ AS
SELECT  КОМПЛ_ID, НАИМЕНОВАНИЕ,  МАРКА, ТУ,  ЕД_ИЗМЕРЕНИЯ, НА_СКЛАДЕ , запрошено, ожидается1- nvl(приб,0) ожидается1,
ожидается2 - nvl(приб,0) ожидается2, ожидается3- nvl(приб,0) ожидается3, ожидается4 - nvl(приб,0) ожидается4
FROM КОМПЛЕКТУЮЩИЕ
left join 
(Select id_выдан as компл_id, sum(Запрошено) as запрошено from "ТРЕБОВАНИЯ" 
where выдано is null
group by id_выдан) USING(компл_id) 
left JOIN
(SELECT компл_id, sum(зая_кол_во) as ожидается1 FROM "ДЕТАЛ_ЗАЯ" JOIN "ЗАЯВКИ" USING ("НОМ_ЗАЯВКИ")
where статус in ('СОСТАВЛЕН','ЗАПРОШЕН','ПОДТВЕРЖДЕН','ОПЛАЧЕН')
group by компл_id) USING(компл_id)
left JOIN
(SELECT компл_id, sum(зая_кол_во) as ожидается2 FROM "ДЕТАЛ_ЗАЯ" JOIN "ЗАЯВКИ" USING ("НОМ_ЗАЯВКИ")
where статус in ('ЗАПРОШЕН','ПОДТВЕРЖДЕН','ОПЛАЧЕН')
group by компл_id) USING(компл_id)
left JOIN
(SELECT компл_id, sum(зая_кол_во) as ожидается3 FROM "ДЕТАЛ_ЗАЯ" JOIN "ЗАЯВКИ" USING ("НОМ_ЗАЯВКИ")
where статус in ('ПОДТВЕРЖДЕН','ОПЛАЧЕН')
group by компл_id) USING(компл_id)
left JOIN
(SELECT компл_id, sum(зая_кол_во) as ожидается4 FROM "ДЕТАЛ_ЗАЯ" JOIN "ЗАЯВКИ" USING ("НОМ_ЗАЯВКИ")
where статус in ('ОПЛАЧЕН')
group by компл_id) USING(компл_id)
left JOIN
(select компл_id, sum(кол_во) as приб from "ЗАЯВКИ" JOIN "НАКЛАДНЫЕ"  using (ном_заявки) JOIN "ДЕТАЛ_НАКЛАДНЫХ" USING (накл_id) where 
статус <> 'ЗАКРЫТ'  group by компл_id) USING(компл_id)
;