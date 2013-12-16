#ifdef __cplusplus

namespace TotalCross
{
	ref class MainViewFactory sealed : Windows::ApplicationModel::Core::IFrameworkViewSource
	{
	public:
		MainViewFactory(Platform::String^ cmdLine);

		virtual Windows::ApplicationModel::Core::IFrameworkView^ CreateView();

	private:
		Platform::String^ cmdLine;
	};
}

#endif