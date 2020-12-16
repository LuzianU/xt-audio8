#ifndef XT_PRIVATE_BLOCKING_STREAM_HPP
#define XT_PRIVATE_BLOCKING_STREAM_HPP

#include <xt/api/private/Stream.hpp>
#include <xt/private/Shared.hpp>
#include <mutex>
#include <atomic>
#include <cstdint>
#include <condition_variable>

#define XT_IMPLEMENT_BLOCKING_STREAM(s)                  \
  XtFault StopStream() override;                         \
  XtFault StartStream() override;                        \
  XtFault ProcessBuffer(bool prefill) override;          \
  XtFault GetFrames(int32_t* frames) const override;     \
  XtFault GetLatency(XtLatency* latency) const override; \
  XtSystem GetSystem() const override { return XtSystem##s; }

struct XtBlockingStream:
public XtStream 
{
  enum class State 
  { 
    Stopped, Starting, Started,
    Stopping, Closing, Closed 
  };

  int32_t _index;
  bool _received;
  bool _aggregated;
  std::mutex _lock;
  bool const _secondary;
  std::atomic<State> _state;
  std::condition_variable _control;
  std::condition_variable _respond;
  static inline int const WaitTimeoutMs = 10000;

  ~XtBlockingStream();
  XtBlockingStream(bool secondary);

  virtual XtFault Stop() override final;
  virtual XtFault Start() override final;
  virtual void OnXRun() const override final;
  virtual XtBool IsRunning() const override final;

  virtual XtFault StopStream() = 0;
  virtual XtFault StartStream() = 0;  
  virtual XtFault ProcessBuffer(bool prefill) = 0;

  void SendControl(State from);
  void ReceiveControl(State state);
  static void RunBlockingStream(XtBlockingStream* stream);
};

#endif // XT_PRIVATE_BLOCKING_STREAM_HPP