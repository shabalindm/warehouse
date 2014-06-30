

-- Запрет изменять значение НОМ_ЗАЯВКИ в таблице ТРЕБОВАНИЯ, если соответсвующая заявка закрыта
create or replace TRIGGER ТРЕБ_TRG1 
BEFORE INSERT OR UPDATE OF НОМ_ЗАЯВКИ OR DELETE ON ТРЕБОВАНИЯ 
for EACH ROW 
DECLARE
-- Для каждого ряда, подвергающиегося DML. Вибираем из таблицы заявок значения флага возможности добавление 
--или удаления новых строчек, ссылающихся на Ном_заявки. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
   if  :new.Ном_заявки = :old.Ном_заявки then -- Если в записи раньше стоял номер заявки и он не менялся - ничего не делаем. 
      null;
   else  -- если это не так - берем те значения номера заявки, которые not null и вытаскивам значения флагов для них
      begin
         SELECT ЗАПР_НА_ИЗМ into restrict_old  FROM ЗАЯВКИ  where Ном_заявки = :old.Ном_заявки;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ЗАПР_НА_ИЗМ into restrict_new  FROM ЗАЯВКИ  where Ном_заявки = :new.Ном_заявки; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- если что-то вытащилось (т.е, стоит запрет) - возбуждаем ошибку
      if restrict_old is not null then
          raise_application_error(-20500, 'Состав заявки '|| :old.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');
      elsif restrict_new is not null  then
          raise_application_error(-20500, 'Состав заявки '|| :new.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');  
      end if; 
    end if;
END ;
/

create or replace trigger Д_НАКЛ_trg2
--Обновляет количество "НА_СКАДЕ" в таблице "КОМПЛЕКТУЮЩИЕ" при изменении таблицы "ДЕТАЛИЗАЦИЯ_НАЛАДНЫХ"
AFTER INSERT or UPDATE or DELETE on ДЕТАЛ_НАКЛАДНЫХ 
for each row
DECLARE
v_added_ID number;
v_added_Qty number;
v_removed_ID number;
v_removed_Qty number;
begin
  if inserting then 
    v_added_ID := :NEW.КОМПЛ_ID;
    v_added_Qty := :NEW.КОЛ_ВО;
    v_removed_ID := null;
    v_removed_Qty :=  0;
  end if;
 if updating then 
    v_added_ID := :NEW.КОМПЛ_ID;
    v_added_Qty := :NEW.КОЛ_ВО;
    v_removed_ID := :OLD.КОМПЛ_ID;
    v_removed_Qty := :OLD.КОЛ_ВО;
  end if; 
  if deleting then 
    v_added_ID := null;
    v_added_Qty := 0;
    v_removed_ID := :OLD.КОМПЛ_ID;
    v_removed_Qty := :OLD.КОЛ_ВО;
  end if;
    
  UPDATE КОМПЛЕКТУЮЩИЕ SET НА_СКЛАДЕ = НА_СКЛАДЕ + NVL(v_added_Qty, 0) WHERE
    КОМПЛЕКТУЮЩИЕ.КОМПЛ_ID = v_added_ID;
   UPDATE КОМПЛЕКТУЮЩИЕ SET НА_СКЛАДЕ = НА_СКЛАДЕ -  NVL(v_removed_Qty, 0) WHERE
    КОМПЛЕКТУЮЩИЕ.КОМПЛ_ID = v_removed_ID;
end;
/

CREATE OR REPLACE TRIGGER ТРЕБ_TRG3 
-- Автоматом ставит ID выданного равным ID Комплектующего, на которого ссылается заявки
BEFORE INSERT ON ТРЕБОВАНИЯ 
for each row
BEGIN
  if :New.ID_ЗАПРОШ is null then
    select компл_id into :New.ID_ЗАПРОШ from спецификация where 
      спец_id = :NEW.спец_id;
  end if; 
   if :New.ID_Выдан is null then
    select компл_id into :New.ID_Выдан from спецификация where 
      спец_id = :NEW.спец_id;
  end if; 
  if :New.ЗАПРОШЕНО is null then
    select К_ВО_ПО_СПЕЦ into :New.ЗАПРОШЕНО from спецификация where 
      спец_id = :NEW.спец_id;
  end if; 
  
END;
/

create or replace trigger ТРЕБ_trg2
AFTER INSERT or UPDATE or DELETE on ТРЕБОВАНИЯ 
for each row
DECLARE
v_added_ID number;
v_added_Qty number;
v_removed_ID number;
v_removed_Qty number;
begin
  if inserting then 
    v_added_ID := :NEW.ID_ВЫДАН;
    v_added_Qty := :NEW.ВЫДАНО;
    v_removed_ID := null;
    v_removed_Qty :=  0;
  end if;
 if updating then 
    v_added_ID := :NEW.ID_ВЫДАН;
    v_added_Qty := :NEW.ВЫДАНО;
    v_removed_ID := :OLD.ID_ВЫДАН;
    v_removed_Qty := :OLD.ВЫДАНО;
  end if; 
  if deleting then 
    v_added_ID := null;
    v_added_Qty := 0;
    v_removed_ID := :OLD.ID_ВЫДАН;
    v_removed_Qty := :OLD.ВЫДАНО;
  end if;
  
  UPDATE КОМПЛЕКТУЮЩИЕ SET НА_СКЛАДЕ = НА_СКЛАДЕ + NVL( v_removed_Qty, 0) WHERE
    КОМПЛЕКТУЮЩИЕ.КОМПЛ_ID = v_removed_ID;
  UPDATE КОМПЛЕКТУЮЩИЕ SET НА_СКЛАДЕ = НА_СКЛАДЕ - NVL(v_added_Qty, 0) WHERE
    КОМПЛЕКТУЮЩИЕ.КОМПЛ_ID = v_added_ID;
   
end;

/

create or replace TRIGGER ДЗАЯ_TRG1 
BEFORE INSERT OR UPDATE OR DELETE ON Детал_зая 
for EACH ROW 
DECLARE
-- Для каждого ряда, подвергающиегося DML. Вибираем из таблицы заявок значения флага возможности добавление 
--или удаления новых строчек, ссылающихся на Ном_заявки. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
      begin
         SELECT ЗАПР_НА_ИЗМ into restrict_old  FROM ЗАЯВКИ  where Ном_заявки = :old.Ном_заявки;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ЗАПР_НА_ИЗМ into restrict_new  FROM ЗАЯВКИ  where Ном_заявки = :new.Ном_заявки; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- если что-то вытащилось (т.е, стоит запрет) - возбуждаем ошибку
      if restrict_old is not null then
          raise_application_error(-20500, 'Состав заявки '|| :old.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');
      elsif restrict_new is not null  then
          raise_application_error(-20500, 'Состав заявки '|| :new.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');  
      end if; 
END ;
/
create or replace TRIGGER НАКЛ_TRG1 
BEFORE INSERT OR UPDATE OR DELETE ON накладные
for EACH ROW 
DECLARE
-- Для каждого ряда, подвергающиегося DML. Вибираем из таблицы заявок значения флага возможности добавление 
--или удаления новых строчек, ссылающихся на Ном_заявки. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
      begin
         SELECT ЗАПР_НА_ИЗМ into restrict_old  FROM ЗАЯВКИ  where Ном_заявки = :old.Ном_заявки;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ЗАПР_НА_ИЗМ into restrict_new  FROM ЗАЯВКИ  where Ном_заявки = :new.Ном_заявки; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- если что-то вытащилось (т.е, стоит запрет) - возбуждаем ошибку
      if restrict_old is not null then
          raise_application_error(-20500, 'Состав заявки '|| :old.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');
      elsif restrict_new is not null  then
          raise_application_error(-20500, 'Состав заявки '|| :new.Ном_заявки||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');  
      end if; 
END ;
/

create or replace TRIGGER Д_НАКЛ_TRG3
BEFORE INSERT OR UPDATE OR DELETE ON ДЕТАЛ_НАКЛАДНЫХ
for EACH ROW 
DECLARE
-- Для каждого ряда, подвергающиегося DML. Вибираем из таблицы заявок значения флага возможности добавление 
--или удаления новых строчек, ссылающихся на Ном_заявки. 
restrict_old varchar2(16);
v_zaya_id varchar2(40);
restrict_new varchar2(16);
begin
      begin
          SELECT Ном_заявки INTO v_zaya_id FROM "НАКЛАДНЫЕ" where "НАКЛ_ID" = :old."НАКЛ_ID";
         SELECT ЗАПР_НА_ИЗМ into restrict_old  FROM ЗАЯВКИ  where Ном_заявки = v_zaya_id;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
           SELECT Ном_заявки INTO v_zaya_id FROM "НАКЛАДНЫЕ" where "НАКЛ_ID" = :new."НАКЛ_ID";
          SELECT ЗАПР_НА_ИЗМ into restrict_new  FROM ЗАЯВКИ  where Ном_заявки = v_zaya_id; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- если что-то вытащилось (т.е, стоит запрет) - возбуждаем ошибку
      if restrict_old is not null then
          raise_application_error(-20500, 'Состав заявки '|| v_zaya_id||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');
      elsif restrict_new is not null  then
          raise_application_error(-20500, 'Состав заявки '|| v_zaya_id||' закрыт для изменения.
          Снимите флаг запрета в таблице ЗАЯВКИ');  
      end if; 
END ;
/
















CREATE OR REPLACE TRIGGER треб_trg4 
BEFORE INSERT OR UPDATE OF ВЫДАНО ON ТРЕБОВАНИЯ 
FOR EACH ROW 
BEGIN
  if :old.ВЫДАНО is null and :new.ВЫДАНО is not null then
      :new.дата_выдачи := sysdate;
   end if;
END;
/