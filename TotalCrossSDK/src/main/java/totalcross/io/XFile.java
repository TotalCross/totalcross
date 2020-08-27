package totalcross.io;

import java.io.IOException;

public class XFile extends XRandomAccessStream {

   public XFile(String fileName) {

   }

   public XFile open() {
      return this;
   }

   public XFile remove(String fileName) {
      return this;
   }

   public XFile rename(String oldName, String fileName) {
      return this;
   }

   public boolean exists(String fileName) {
      return false;
   }


@Override
public void close() throws IOException {
	// TODO Auto-generated method stub
	
}

@Override
public long pos() throws IOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public void seek(long pos) throws IOException {
	// TODO Auto-generated method stub
	
}

@Override
public long read(byte[] buf, long start, long count) throws IOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public long write(byte[] buf, long start, long count) throws IOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public void reset() throws IOException {
	// TODO Auto-generated method stub
	
}

@Override
public long size() throws IOException {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public CharSequence readLine() throws IOException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public char[] readLine(long maxSize) throws IOException {
	// TODO Auto-generated method stub
	return null;
}




// virtual bool	resize(qint64 sz)
// bool	setFileTime(const QDateTime &newDate, QFileDevice::FileTime fileTime)
// virtual bool	setPermissions(QFileDevice::Permissions permissions)
// virtual QFileDevice::Permissions	permissions() const
// QDateTime	fileTime(QFileDevice::FileTime time) const
// bool	remove(const QString &fileName)
// bool	rename(const QString &oldName, const QString &newName)
// bool	exists(const QString &fileName)
// bool	link(const QString &fileName, const QString &linkName)
// bool	moveToTrash(const QString &fileName, QString *pathInTrash = nullptr)


/* 
   QIODevice {
      flags	OpenMode
      enum	OpenModeFlag { NotOpen, ReadOnly, WriteOnly, ReadWrite, Append, …, ExistingOnly }

      QIODevice(QObject *parent)
      QIODevice()
      virtual	~QIODevice()
      virtual bool	atEnd() const
      virtual qint64	bytesAvailable() const
      virtual qint64	bytesToWrite() const
      virtual bool	canReadLine() const
      virtual void	close()
      void	commitTransaction()
      int	currentReadChannel() const
      int	currentWriteChannel() const
      QString	errorString() const
      bool	getChar(char *c)
      bool	isOpen() const
      bool	isReadable() const
      virtual bool	isSequential() const
      bool	isTextModeEnabled() const
      bool	isTransactionStarted() const
      bool	isWritable() const
      virtual bool	open(QIODevice::OpenMode mode)
      QIODevice::OpenMode	openMode() const
      qint64	peek(char *data, qint64 maxSize)
      QByteArray	peek(qint64 maxSize)
      virtual qint64	pos() const
      bool	putChar(char c)
      qint64	read(char *data, qint64 maxSize)
      QByteArray	read(qint64 maxSize)
      QByteArray	readAll()
      int	readChannelCount() const
      qint64	readLine(char *data, qint64 maxSize)
      QByteArray	readLine(qint64 maxSize = 0)
      virtual bool	reset()
      void	rollbackTransaction()
      virtual bool	seek(qint64 pos)
      void	setCurrentReadChannel(int channel)
      void	setCurrentWriteChannel(int channel)
      void	setTextModeEnabled(bool enabled)
      virtual qint64	size() const
      qint64	skip(qint64 maxSize)
      void	startTransaction()
      void	ungetChar(char c)
      virtual bool	waitForBytesWritten(int msecs)
      virtual bool	waitForReadyRead(int msecs)
      qint64	write(const char *data, qint64 maxSize)
      qint64	write(const char *data)
      qint64	write(const QByteArray &byteArray)
      int	writeChannelCount() const

      // Signals
      void	aboutToClose()
      void	bytesWritten(qint64 bytes)
      void	channelBytesWritten(int channel, qint64 bytes)
      void	channelReadyRead(int channel)
      void	readChannelFinished()
      void	readyRead()

      virtual qint64	readData(char *data, qint64 maxSize) = 0
      virtual qint64	readLineData(char *data, qint64 maxSize)
      void	setErrorString(const QString &str)
      void	setOpenMode(QIODevice::OpenMode openMode)
      virtual qint64	writeData(const char *data, qint64 maxSize) = 0
   }

   QFileDevice {
      enum	FileError { NoError, ReadError, WriteError, FatalError, ResourceError, …, CopyError }
      enum	FileHandleFlag { AutoCloseHandle, DontCloseHandle }
      flags	FileHandleFlags
      enum	FileTime { FileAccessTime, FileBirthTime, FileMetadataChangeTime, FileModificationTime }
      enum	MemoryMapFlags { NoOptions, MapPrivateOption }
      enum	Permission { ReadOwner, WriteOwner, ExeOwner, ReadUser, WriteUser, …, ExeOther }
      flags	Permissions

      virtual	~QFileDevice()
      QFileDevice::FileError	error() const
      virtual QString	fileName() const
      QDateTime	fileTime(QFileDevice::FileTime time) const
      bool	flush()
      int	handle() const
      uchar *	map(qint64 offset, qint64 size, QFileDevice::MemoryMapFlags flags = NoOptions)
      virtual QFileDevice::Permissions	permissions() const
      virtual bool	resize(qint64 sz)
      bool	setFileTime(const QDateTime &newDate, QFileDevice::FileTime fileTime)
      virtual bool	setPermissions(QFileDevice::Permissions permissions)
      bool	unmap(uchar *address)
      void	unsetError()

      virtual bool	atEnd() const override
      virtual void	close() override
      virtual bool	isSequential() const override
      virtual qint64	pos() const override
      virtual bool	seek(qint64 pos) override
      virtual qint64	size() const override

      virtual qint64	readData(char *data, qint64 len) override
      virtual qint64	readLineData(char *data, qint64 maxlen) override
      virtual qint64	writeData(const char *data, qint64 len) override
   }



	QFile(const QString &name, QObject *parent);
	QFile(QObject *parent);
	QFile(const QString &name);
	QFile();
	virtual	~QFile();
	bool	copy(const QString &newName);
	bool	exists() const;
	bool	link(const QString &linkName)
	bool	moveToTrash()
	bool	open(FILE *fh, QIODevice::OpenMode mode, QFileDevice::FileHandleFlags handleFlags = DontCloseHandle)
	bool	open(int fd, QIODevice::OpenMode mode, QFileDevice::FileHandleFlags handleFlags = DontCloseHandle)
	bool	remove()
	bool	rename(const QString &newName)
	void	setFileName(const QString &name)
	QString	symLinkTarget() const

	virtual QString	fileName() const override
	virtual bool	open(QIODevice::OpenMode mode) override
	virtual QFileDevice::Permissions	permissions() const override
	virtual bool	resize(qint64 sz) override
	virtual bool	setPermissions(QFileDevice::Permissions permissions) override
	virtual qint64	size() const override

	bool	copy(const QString &fileName, const QString &newName)
	QString	decodeName(const QByteArray &localFileName)
	QString	decodeName(const char *localFileName)
	QByteArray	encodeName(const QString &fileName)
	bool	exists(const QString &fileName)
	bool	link(const QString &fileName, const QString &linkName)
	bool	moveToTrash(const QString &fileName, QString *pathInTrash = nullptr)
	QFileDevice::Permissions	permissions(const QString &fileName)
	bool	remove(const QString &fileName)
	bool	rename(const QString &oldName, const QString &newName)
	bool	resize(const QString &fileName, qint64 sz)
	bool	setPermissions(const QString &fileName, QFileDevice::Permissions permissions)
   QString	symLinkTarget(const QString &fileName)
    */
}