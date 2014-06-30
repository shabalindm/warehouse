create or replace PROCEDURE CREATE_AUTOINCREMENT 
(
  TABLE_NAME IN VARCHAR2 -- ��� �������, ��� ������� ��������� �������������
, ID_COL_NAME IN VARCHAR2 -- ��� �������, ��� �������� ��������� �������������
, trg_head IN VARCHAR2 -- �������, ������� ����� �������������� ��� ����� ������� � �������������������
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
       raise_application_error (-20555, ''�� ���� ������ �������� ���������� �����''); 
     end if;
  end if;
  ' ||
  additional_acts || '
end;' ;
END CREATE_AUTOINCREMENT;
/

CREATE TABLE ������������� 
(   �����_ID NUMBER NOT NULL 
  , ������ VARCHAR2(100)  
  , ������������ VARCHAR2(200) 
  , �� VARCHAR2(100) 
  , ����� VARCHAR2(100) 
  , ��_��������� VARCHAR2(40) default '��'
  , ��_������ NUMBER(7,3) DEFAULT 0
  , ����_���� varchar2(400)
  , ����_���� DATE DEFAULT SYSDATE
  , ���� NUMBER(10,2) 
  , CONSTRAINT �����_�� PRIMARY KEY ( �����_ID ) ENABLE 
  , CONSTRAINT �����_����_��� UNIQUE ( ��, �����) ENABLE
  , CONSTRAINT "�����_���>0" CHECK  (��_������ >= 0) ENABLE
);

begin
CREATE_AUTOINCREMENT('�������������', '�����_ID', '�����' , 
':new.����� := upper(trim(:new.�����)); 
  :new.�� := upper(trim(:new.��));'
);
end;
/

drop TABLE ������������; 
CREATE TABLE ������������ 
(
  ����_ID NUMBER NOT NULL 
, ���_����� VARCHAR2(40)
, ���_������ VARCHAR2(40)
, ����_� VARCHAR2(40)
, ������� VARCHAR2(200)
, ���� VARCHAR2(200)
, ���_����� VARCHAR2(40)
, ���_���_�_���� NUMBER(4)
, ����_���� VARCHAR2(400)
, �����_ID number not null CONSTRAINT ����_��_�����id
                REFERENCES �������������(�����_ID) ENABLE
, �_��_��_���� NUMBER(7,3)  DEFAULT 1 CONSTRAINT �_��_NOT_NULL   NOT NULL
, ����_���� DATE DEFAULT sysdate 
, CONSTRAINT ����_�� PRIMARY KEY ( ����_ID) ENABLE 
,CONSTRAINT ����_����_��� UNIQUE (���_�����,  ���_���_�_����) ENABLE
);

begin
 CREATE_AUTOINCREMENT('������������', '����_ID', '���',
'if (:new.���_���_�_���� is null) then   
         :new.���_���_�_���� := -:new.����_ID;
      end if;'
);
end;
/

DROP TABLE ������;
CREATE TABLE ������ 
(
  ���_������ VARCHAR2(40) NOT NULL 
, ����_������ DATE DEFAULT SYSDATE 
, ���_����� VARCHAR2(40) 
, ��_����� DATE 
, ��������� VARCHAR2(200) 
, �����_��� NUMBER(9,2)
, ������ VARCHAR2(40) default '���������'  NOT NULL check ( ������ IN ('���������','��������','�����������','�������', '������' )) 
, ��_������ DATE 
, ����� VARCHAR2(40)
, ������� VARCHAR2(200)
, ������� VARCHAR2(200)
, ���_������ VARCHAR2(40)
, ���_���� VARCHAR2(1000)
, CONSTRAINT �����_�� PRIMARY KEY (���_������)  ENABLE 
, ����_��_���  VARCHAR2(16) check ( ����_��_��� IN ( '�������' ))
);

 
DROP TABLE ����������; 
CREATE TABLE ���������� 
( 
   ����_ID NUMBER NOT NULL 
  ,���_���� VARCHAR2(40) NOT NULL
  ,����_ID NUMBER not null CONSTRAINT ����_��_����
                REFERENCES ������������(����_ID)   ENABLE
  ,���_������ VARCHAR2(40)  CONSTRAINT �_���_��_���
             REFERENCES ������(���_������) ON DELETE SET NULL ENABLE
  ,��������� NUMBER(7,3) 
  ,������ NUMBER(7,3) 
  ,ID_������ NUMBER not null CONSTRAINT ���_��_�_ID
              REFERENCES �������������(�����_ID)  ENABLE
  ,ID_����� NUMBER not null CONSTRAINT ���_��_�_ID1
              REFERENCES �������������(�����_ID)  ENABLE
  ,����_���� DATE DEFAULT SYSDATE
  ,����_������ DATE
  ,����_���� VARCHAR2(1000)
  ,CONSTRAINT ����������_�� PRIMARY KEY 
   (����_ID)  ENABLE 
);

begin
 CREATE_AUTOINCREMENT('����������', '����_ID', '����');
end;
/

DROP table �����_���;
create table �����_��� (
  �����_���_ID NUMBER(6)
,���_������ VARCHAR(40) CONSTRAINT �_�_��_�_ID
              REFERENCES ������(���_������)  ENABLE
,�����_id CONSTRAINT �_�_��_�_ID
              REFERENCES �������������(�����_id)  ENABLE
,���_���_�� NUMBER(7,3)
,���������_��� NUMBER(9,2)
);
execute CREATE_AUTOINCREMENT('�����_���', '�����_���_ID', '���');

DROP TABLE ���������;
CREATE TABLE ��������� 
(
  ����_ID NUMBER NOT NULL PRIMARY KEY 
, ���_������ VARCHAR2(40) NOT NULL CONSTRAINT ����_��_�_�
               REFERENCES ������(���_������) ENABLE
, ���_���_���� VARCHAR2(100) 
, ��_���_���� DATE DEFAULT SYSDATE
, ���_�� VARCHAR2(100)
);

execute CREATE_AUTOINCREMENT('���������', '����_ID', '����');


DROP TABLE �����_���������;
CREATE TABLE �����_��������� 
(
  �_����_ID NUMBER NOT NULL PRIMARY KEY
  ,����_ID NUMBER NOT NULL CONSTRAINT �_����_��_�_ID
               REFERENCES ���������(����_ID) ON DELETE CASCADE  ENABLE
 , �����_ID NUMBER NOT NULL CONSTRAINT �_����_��_�_ID
               REFERENCES �������������(�����_ID)  ENABLE
 , ���_�� NUMBER(7,3)  
);

execute CREATE_AUTOINCREMENT('�����_���������', '�_����_ID', '�_����');


CREATE OR REPLACE VIEW ������� AS
SELECT  �����_ID, ������������,  �����, ��,  ��_���������, ��_������ , ���������, ���������1- nvl(����,0) ���������1,
���������2 - nvl(����,0) ���������2, ���������3- nvl(����,0) ���������3, ���������4 - nvl(����,0) ���������4
FROM �������������
left join 
(Select id_����� as �����_id, sum(���������) as ��������� from "����������" 
where ������ is null
group by id_�����) USING(�����_id) 
left JOIN
(SELECT �����_id, sum(���_���_��) as ���������1 FROM "�����_���" JOIN "������" USING ("���_������")
where ������ in ('���������','��������','�����������','�������')
group by �����_id) USING(�����_id)
left JOIN
(SELECT �����_id, sum(���_���_��) as ���������2 FROM "�����_���" JOIN "������" USING ("���_������")
where ������ in ('��������','�����������','�������')
group by �����_id) USING(�����_id)
left JOIN
(SELECT �����_id, sum(���_���_��) as ���������3 FROM "�����_���" JOIN "������" USING ("���_������")
where ������ in ('�����������','�������')
group by �����_id) USING(�����_id)
left JOIN
(SELECT �����_id, sum(���_���_��) as ���������4 FROM "�����_���" JOIN "������" USING ("���_������")
where ������ in ('�������')
group by �����_id) USING(�����_id)
left JOIN
(select �����_id, sum(���_��) as ���� from "������" JOIN "���������"  using (���_������) JOIN "�����_���������" USING (����_id) where 
������ <> '������'  group by �����_id) USING(�����_id)
;