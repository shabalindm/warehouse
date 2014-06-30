

-- ������ �������� �������� ���_������ � ������� ����������, ���� �������������� ������ �������
create or replace TRIGGER ����_TRG1 
BEFORE INSERT OR UPDATE OF ���_������ OR DELETE ON ���������� 
for EACH ROW 
DECLARE
-- ��� ������� ����, ���������������� DML. �������� �� ������� ������ �������� ����� ����������� ���������� 
--��� �������� ����� �������, ����������� �� ���_������. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
   if  :new.���_������ = :old.���_������ then -- ���� � ������ ������ ����� ����� ������ � �� �� ������� - ������ �� ������. 
      null;
   else  -- ���� ��� �� ��� - ����� �� �������� ������ ������, ������� not null � ���������� �������� ������ ��� ���
      begin
         SELECT ����_��_��� into restrict_old  FROM ������  where ���_������ = :old.���_������;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ����_��_��� into restrict_new  FROM ������  where ���_������ = :new.���_������; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- ���� ���-�� ���������� (�.�, ����� ������) - ���������� ������
      if restrict_old is not null then
          raise_application_error(-20500, '������ ������ '|| :old.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');
      elsif restrict_new is not null  then
          raise_application_error(-20500, '������ ������ '|| :new.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');  
      end if; 
    end if;
END ;
/

create or replace trigger �_����_trg2
--��������� ���������� "��_�����" � ������� "�������������" ��� ��������� ������� "�����������_��������"
AFTER INSERT or UPDATE or DELETE on �����_��������� 
for each row
DECLARE
v_added_ID number;
v_added_Qty number;
v_removed_ID number;
v_removed_Qty number;
begin
  if inserting then 
    v_added_ID := :NEW.�����_ID;
    v_added_Qty := :NEW.���_��;
    v_removed_ID := null;
    v_removed_Qty :=  0;
  end if;
 if updating then 
    v_added_ID := :NEW.�����_ID;
    v_added_Qty := :NEW.���_��;
    v_removed_ID := :OLD.�����_ID;
    v_removed_Qty := :OLD.���_��;
  end if; 
  if deleting then 
    v_added_ID := null;
    v_added_Qty := 0;
    v_removed_ID := :OLD.�����_ID;
    v_removed_Qty := :OLD.���_��;
  end if;
    
  UPDATE ������������� SET ��_������ = ��_������ + NVL(v_added_Qty, 0) WHERE
    �������������.�����_ID = v_added_ID;
   UPDATE ������������� SET ��_������ = ��_������ -  NVL(v_removed_Qty, 0) WHERE
    �������������.�����_ID = v_removed_ID;
end;
/

CREATE OR REPLACE TRIGGER ����_TRG3 
-- ��������� ������ ID ��������� ������ ID ��������������, �� �������� ��������� ������
BEFORE INSERT ON ���������� 
for each row
BEGIN
  if :New.ID_������ is null then
    select �����_id into :New.ID_������ from ������������ where 
      ����_id = :NEW.����_id;
  end if; 
   if :New.ID_����� is null then
    select �����_id into :New.ID_����� from ������������ where 
      ����_id = :NEW.����_id;
  end if; 
  if :New.��������� is null then
    select �_��_��_���� into :New.��������� from ������������ where 
      ����_id = :NEW.����_id;
  end if; 
  
END;
/

create or replace trigger ����_trg2
AFTER INSERT or UPDATE or DELETE on ���������� 
for each row
DECLARE
v_added_ID number;
v_added_Qty number;
v_removed_ID number;
v_removed_Qty number;
begin
  if inserting then 
    v_added_ID := :NEW.ID_�����;
    v_added_Qty := :NEW.������;
    v_removed_ID := null;
    v_removed_Qty :=  0;
  end if;
 if updating then 
    v_added_ID := :NEW.ID_�����;
    v_added_Qty := :NEW.������;
    v_removed_ID := :OLD.ID_�����;
    v_removed_Qty := :OLD.������;
  end if; 
  if deleting then 
    v_added_ID := null;
    v_added_Qty := 0;
    v_removed_ID := :OLD.ID_�����;
    v_removed_Qty := :OLD.������;
  end if;
  
  UPDATE ������������� SET ��_������ = ��_������ + NVL( v_removed_Qty, 0) WHERE
    �������������.�����_ID = v_removed_ID;
  UPDATE ������������� SET ��_������ = ��_������ - NVL(v_added_Qty, 0) WHERE
    �������������.�����_ID = v_added_ID;
   
end;

/

create or replace TRIGGER ����_TRG1 
BEFORE INSERT OR UPDATE OR DELETE ON �����_��� 
for EACH ROW 
DECLARE
-- ��� ������� ����, ���������������� DML. �������� �� ������� ������ �������� ����� ����������� ���������� 
--��� �������� ����� �������, ����������� �� ���_������. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
      begin
         SELECT ����_��_��� into restrict_old  FROM ������  where ���_������ = :old.���_������;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ����_��_��� into restrict_new  FROM ������  where ���_������ = :new.���_������; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- ���� ���-�� ���������� (�.�, ����� ������) - ���������� ������
      if restrict_old is not null then
          raise_application_error(-20500, '������ ������ '|| :old.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');
      elsif restrict_new is not null  then
          raise_application_error(-20500, '������ ������ '|| :new.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');  
      end if; 
END ;
/
create or replace TRIGGER ����_TRG1 
BEFORE INSERT OR UPDATE OR DELETE ON ���������
for EACH ROW 
DECLARE
-- ��� ������� ����, ���������������� DML. �������� �� ������� ������ �������� ����� ����������� ���������� 
--��� �������� ����� �������, ����������� �� ���_������. 
restrict_old varchar2(16);
restrict_new varchar2(16);
begin
      begin
         SELECT ����_��_��� into restrict_old  FROM ������  where ���_������ = :old.���_������;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
          SELECT ����_��_��� into restrict_new  FROM ������  where ���_������ = :new.���_������; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- ���� ���-�� ���������� (�.�, ����� ������) - ���������� ������
      if restrict_old is not null then
          raise_application_error(-20500, '������ ������ '|| :old.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');
      elsif restrict_new is not null  then
          raise_application_error(-20500, '������ ������ '|| :new.���_������||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');  
      end if; 
END ;
/

create or replace TRIGGER �_����_TRG3
BEFORE INSERT OR UPDATE OR DELETE ON �����_���������
for EACH ROW 
DECLARE
-- ��� ������� ����, ���������������� DML. �������� �� ������� ������ �������� ����� ����������� ���������� 
--��� �������� ����� �������, ����������� �� ���_������. 
restrict_old varchar2(16);
v_zaya_id varchar2(40);
restrict_new varchar2(16);
begin
      begin
          SELECT ���_������ INTO v_zaya_id FROM "���������" where "����_ID" = :old."����_ID";
         SELECT ����_��_��� into restrict_old  FROM ������  where ���_������ = v_zaya_id;
         EXCEPTION  WHEN NO_DATA_FOUND THEN    NULL;
      end;
      begin
           SELECT ���_������ INTO v_zaya_id FROM "���������" where "����_ID" = :new."����_ID";
          SELECT ����_��_��� into restrict_new  FROM ������  where ���_������ = v_zaya_id; 
          EXCEPTION     WHEN NO_DATA_FOUND THEN    NULL;
      end;
      -- ���� ���-�� ���������� (�.�, ����� ������) - ���������� ������
      if restrict_old is not null then
          raise_application_error(-20500, '������ ������ '|| v_zaya_id||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');
      elsif restrict_new is not null  then
          raise_application_error(-20500, '������ ������ '|| v_zaya_id||' ������ ��� ���������.
          ������� ���� ������� � ������� ������');  
      end if; 
END ;
/
















CREATE OR REPLACE TRIGGER ����_trg4 
BEFORE INSERT OR UPDATE OF ������ ON ���������� 
FOR EACH ROW 
BEGIN
  if :old.������ is null and :new.������ is not null then
      :new.����_������ := sysdate;
   end if;
END;
/