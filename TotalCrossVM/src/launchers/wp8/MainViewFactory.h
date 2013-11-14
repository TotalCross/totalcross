#ifdef __cplusplus

namespace TotalCross
{
	using namespace Platform;

	ref class MainViewFactory sealed : Windows::ApplicationModel::Core::IFrameworkViewSource
	{
	public:
		MainViewFactory(String^ cmdLine);

		virtual Windows::ApplicationModel::Core::IFrameworkView^ CreateView();

	private:
		String^ cmdLine;
	};
}

#endif