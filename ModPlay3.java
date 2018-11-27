
public class ModPlay3
{
	private static final int MAX_SAMPLES = 32;
	private static final int MAX_CHANNELS = 16;
	private static final int FIXED_POINT_SHIFT = 13;
	private static final int FIXED_POINT_ONE = 1 << FIXED_POINT_SHIFT;
	
	private static final int[] FILTER_COEFFS =
	{
		-512, 0, 4096, 8192, 4096, 0, -512
	};
	
	private static final short[] FINE_TUNE =
	{
		8192, 8251, 8311, 8371, 8432, 8493, 8555, 8617,
		7732, 7788, 7845, 7902, 7959, 8016, 8075, 8133
	};
	
	private static final short[] ARPEGGIO =
	{
		8192, 7732, 7298, 6889, 6502, 6137, 5793, 5468,
		5161, 4871, 4598, 4340, 4096, 3866, 3649, 3444
	};
	
	private static final short[] VIBRATO =
	{
		0,    24,  49,  74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
		255, 253, 250, 244,235, 224, 212, 197, 180, 161, 141, 120,  97,  74,  49,  24
	};
	
	private int sampleRate;
	private String songName;
	private String[] instrumentNames = new String[ MAX_SAMPLES ];
	private int numChannels;
	private int songLength;
	private int numPatterns;
	private int c2Rate;
	private byte[] sequence;
	private byte[] patternData;
	private byte[][] sampleData = new byte[ MAX_SAMPLES ][];
	private int[] sampleFineTune = new int[ MAX_SAMPLES ];
	private int[] sampleVolume = new int[ MAX_SAMPLES ];
	private int[] sampleLoopStart = new int[ MAX_SAMPLES ];
	private int[] sampleLoopLength = new int[ MAX_SAMPLES ];
	private int[] channelInstrument = new int[ MAX_CHANNELS ];
	private int[] channelAssigned = new int[ MAX_CHANNELS ];
	private int[] channelEffect = new int[ MAX_CHANNELS ];
	private int[] channelParameter = new int[ MAX_CHANNELS ];
	private int[] channelVolume = new int[ MAX_CHANNELS ];
	private int[] channelPanning = new int[ MAX_CHANNELS ];
	private int[] channelPeriod = new int[ MAX_CHANNELS ];
	private int[] channelSamplePos = new int[ MAX_CHANNELS ];
	private int[] channelStep = new int[ MAX_CHANNELS ];
	private int[] channelArpeggio = new int[ MAX_CHANNELS ];
	private int[] channelVibrato = new int[ MAX_CHANNELS ];
	private int[] channelVibratoSpeed = new int[ MAX_CHANNELS ];
	private int[] channelVibratoDepth = new int[ MAX_CHANNELS ];
	private int[] channelVibratoPhase = new int[ MAX_CHANNELS ];
	private int[] channelPortaPeriod = new int[ MAX_CHANNELS ];
	private int[] channelPortaSpeed = new int[ MAX_CHANNELS ];
	private int[] channelTremolo = new int[ MAX_CHANNELS ];
	private int[] channelTremoloSpeed = new int[ MAX_CHANNELS ];
	private int[] channelTremoloDepth = new int[ MAX_CHANNELS ];
	private int[] channelPatternLoopRow = new int[ MAX_CHANNELS ];
	private int currentSequencePos;
	private int nextSequencePos;
	private int currentRow;
	private int nextRow;
	private int currentTick;
	private int ticksPerRow = 6;
	private int tempo = 125;
	private int effectCounter;
	private int patternLoopCount;
	private int patternLoopChannel;
	private int tickSamplePos;
	private int tickSampleLen;
	
	/* If 'soundtracker' is true, the Module Data is assumed to be in the original Ultimate Soundtracker format. */
	public ModPlay3( int sampleRate, java.io.InputStream moduleData, boolean soundtracker ) throws java.io.IOException
	{
		this.sampleRate = sampleRate;
		songName = readString( moduleData, 20 );
		int numSamples = soundtracker ? 16 : 32;
		int[] sampleLengths = new int[ numSamples ];
		for( int idx = 1; idx < numSamples; idx++ )
		{
			instrumentNames[ idx ] = readString( moduleData, 22 );
			sampleLengths[ idx ] = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			sampleFineTune[ idx ] = moduleData.read() & ( soundtracker ? 0 : 0xF );
			sampleVolume[ idx ] = moduleData.read() & 0x7F;
			int loopStart = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * ( soundtracker ? 1 : 2 );
			int loopLength = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			if( loopLength < 4 || loopStart > sampleLengths[ idx ] )
			{
				loopStart = sampleLengths[ idx ];
			}
			if( loopStart + loopLength > sampleLengths[ idx ] )
			{
				loopLength = sampleLengths[ idx ] - loopStart;
			}
			sampleLoopStart[ idx ] = loopStart * FIXED_POINT_ONE;
			sampleLoopLength[ idx ] = loopLength * FIXED_POINT_ONE;
		}
		songLength = moduleData.read() & 0x7F;
		int restart = moduleData.read() & 0x7F;
		sequence = readBytes( moduleData, 128 );
		for( int idx = 0; idx < 128; idx++ )
		{
			if( numPatterns < sequence[ idx ] + 1 ) 
			{
				numPatterns = sequence[ idx ] + 1;
			}
		}
		String modType = soundtracker ? "M.K." : readString( moduleData, 4 );
		if( modType.equals( "M.K." ) )
		{
			numChannels = 4;
			c2Rate = 8287;
		}
		else
		{
			throw new IllegalArgumentException( "Module not recognised!" );
		}
		patternData = readBytes( moduleData, numChannels * 4 * 64 * numPatterns );
		if( soundtracker )
		{
			for( int idx = 0; idx < patternData.length; idx += 4 )
			{
				int key = ( ( patternData[ idx ] & 0xF ) << 8 ) | ( patternData[ idx + 1 ] & 0xFF );
				int instrument = ( patternData[ idx + 2 ] >> 4 ) & 0xF;
				int effect = patternData[ idx + 2 ] & 0xF;
				int param1 = ( patternData[ idx + 3 ] >> 4 ) & 0xF;
				int param2 = patternData[ idx + 3 ] & 0xF;
				if( effect == 1 )
				{
					effect = 0;
				}
				else if( effect == 2 && ( param1 - param2 ) < 0 )
				{
					effect = 1;
					param2 = param2 - param1;
					param1 = 0;
				} 
				else if( effect == 2 )
				{
					effect = 2;
					param2 = param1 - param2;
					param1 = 0;
				}
				else
				{
					effect = param1 = param2 = 0;
				}
				patternData[ idx ] = ( byte ) ( ( key >> 8 ) & 0xF );
				patternData[ idx + 1 ] = ( byte ) ( key & 0xFF );
				patternData[ idx + 2 ] = ( byte ) ( ( instrument << 4 ) | ( effect & 0xF ) );
				patternData[ idx + 3 ] = ( byte ) ( ( param1 << 4 ) | ( param2 & 0xF ) );
			}
		}
		for( int idx = 1; idx < numSamples; idx++ )
		{
			sampleData[ idx ] = readBytes( moduleData, sampleLengths[ idx ] );
		}
		for( int chn = 0; chn < MAX_CHANNELS; chn += 4 )
		{
			channelPanning[ chn ] = channelPanning[ chn + 3 ] = FIXED_POINT_ONE / 4;
			channelPanning[ chn + 1 ] = channelPanning[ chn + 2 ] = FIXED_POINT_ONE / 4 * 3;
		}
	}
	
	public void getAudio( int[] output, int offset, int count )
	{
		for( int idx = offset * 2, end = ( offset + count ) * 2; idx < end; idx += 2 )
		{
			if( tickSamplePos++ >= tickSampleLen )
			{
				tick();
				tickSamplePos = 0;
				tickSampleLen = ( sampleRate * 5 ) / ( tempo * 2 );
			}
			int lamp = 0, ramp = 0;
			for( int chn = 0; chn < numChannels; chn++ )
			{
				int instrument = channelInstrument[ chn ];
				int loopLength = sampleLoopLength[ instrument ];
				int loopEnd = sampleLoopStart[ instrument ] + loopLength;
				int samplePos = channelSamplePos[ chn ];
				if( samplePos < loopEnd )
				{
					int amplitude = sampleData[ instrument ][ samplePos >> FIXED_POINT_SHIFT ] * channelVolume[ chn ];
					lamp += ( amplitude * ( FIXED_POINT_ONE - channelPanning[ chn ] ) ) >> FIXED_POINT_SHIFT;
					ramp += ( amplitude * channelPanning[ chn ] ) >> FIXED_POINT_SHIFT;
					samplePos += channelStep[ chn ];
					if( loopLength > 0 )
					{
						while( samplePos >= loopEnd )
						{
							samplePos -= loopLength;
						}
					}
					channelSamplePos[ chn ] = samplePos;
				}
			}
			output[ idx ] = lamp;
			output[ idx + 1 ] = ramp;
		}
	}
	
	private void row()
	{
		boolean patternBreak = false;
		if( nextSequencePos < songLength )
		{
			currentSequencePos = nextSequencePos;
		}
		else
		{
			currentSequencePos = nextSequencePos = 0;
		}
		if( nextRow < 64 )
		{
			currentRow = nextRow;
		}
		else
		{
			currentRow = 0;
		}
		currentTick = ticksPerRow;
		nextRow = currentRow + 1;
		if( nextRow > 63 )
		{
			nextRow = 0;
			nextSequencePos = currentSequencePos + 1;
			patternBreak = true;
		}
		for( int chn = 0; chn < numChannels; chn++ )
		{
			int rowOffset = sequence[ currentSequencePos ] * 64 + currentRow;
			int patternDataOffset = ( rowOffset * numChannels + chn ) * 4;
			int period = ( ( patternData[ patternDataOffset ] & 0xF ) << 8 ) | ( patternData[ patternDataOffset + 1 ] & 0xFF );
			int instrument = ( patternData[ patternDataOffset ] & 0x10 ) | ( ( patternData[ patternDataOffset + 2 ] & 0xF0 ) >> 4 );
			int effect = patternData[ patternDataOffset + 2 ] & 0xF;
			int parameter = patternData[ patternDataOffset + 3 ] & 0xFF;
			if( effect == 0xE )
			{
				effect = 0xE0 | ( ( parameter >> 4 ) & 0xF );
				parameter = parameter & 0xF;
			}
			if( instrument > 0 )
			{
				channelAssigned[ chn ] = instrument;
				channelVolume[ chn ] = sampleVolume[ instrument ];
				if( channelSamplePos[ chn ] >= sampleLoopStart[ channelInstrument[ chn ] ]
					&& sampleLoopLength[ instrument ] > 0 && channelInstrument[ chn ] > 0 )
				{
					channelSamplePos[ chn ] -= sampleLoopStart[ channelInstrument[ chn ] ];
					channelSamplePos[ chn ] = sampleLoopStart[ instrument ] + channelSamplePos[ chn ] % sampleLoopLength[ instrument ];
					channelInstrument[ chn ] = instrument;
				}
			}
			if( period > 0 )
			{
				channelPortaPeriod[ chn ] = period;
				if( effect != 0x3 && effect != 0x5 && effect != 0xED )
				{
					channelInstrument[ chn ] = channelAssigned[ chn ];
					channelPeriod[ chn ] = period;
					channelSamplePos[ chn ] = 0;
					channelVibratoPhase[ chn ] = 0;
				}
			}
			channelEffect[ chn ] = effect;
			channelParameter[ chn ] = parameter;
			channelArpeggio[ chn ] = 0;
			channelVibrato[ chn ] = 0;
			channelTremolo[ chn ] = 0;
			switch( effect )
			{
				case 0x0: /* Arpeggio. */
				case 0x1: /* Portamento up. */
				case 0x2: /* Portamento down. */
					break;
				case 0x3: /* Tone portamento. */
					if( parameter > 0 )
					{
						channelPortaSpeed[ chn ] = parameter;
					}
					break;
				case 0x4: /* Vibrato. */
					if( ( parameter & 0xF0 ) > 0 )
					{
						channelVibratoSpeed[ chn ] = ( parameter & 0xF0 ) >> 4;
					}
					if( ( parameter & 0xF ) > 0 )
					{
						channelVibratoDepth[ chn ] = parameter & 0xF;
					}
					vibrato( chn );
					break;
				case 0x5: /* Tone porta + Volume slide. */
					break;
				case 0x6: /* Vibrato + Volume slide. */
					vibrato( chn );
					break;
				case 0x7: /* Tremolo. */
					if( ( parameter & 0xF0 ) > 0 )
					{
						channelTremoloSpeed[ chn ] = ( parameter & 0xF0 ) >> 4;
					}
					if( ( parameter & 0xF ) > 0 )
					{
						channelTremoloDepth[ chn ] = parameter & 0xF;
					}
					tremolo( chn );
					break;
				case 0x8: /* Set panning. */
					if( numChannels > 4 )
					{
						if( parameter > 128 )
						{
							channelPanning[ chn ] = FIXED_POINT_ONE / 2;
						}
						else
						{
							channelPanning[ chn ] = parameter * FIXED_POINT_ONE / 128;
						}
					}
					break;
				case 0x9: /* Set sample offset. */
					channelSamplePos[ chn ] = parameter * 256 * FIXED_POINT_ONE;
					break;
				case 0xA: /* Volume slide. */
					break;
				case 0xB: /* Pattern jump. */
					nextSequencePos = parameter;
					if( !patternBreak )
					{
						nextRow = 0;
						patternBreak = true;
					}
					break;
				case 0xC: /* Set volume. */
					channelVolume[ chn ] = parameter;
					break;
				case 0xD: /* Pattern break. */
					if( !patternBreak )
					{
						nextSequencePos = currentSequencePos + 1;
						patternBreak = true;
					}
					nextRow = ( parameter >> 4 ) * 10 + ( parameter & 0xF );
					break;
				case 0xE: /* Remapped to 0xEx. */
					break;
				case 0xF: /* Set speed/tempo.*/
					if( channelParameter[ chn ] > 31 )
					{
						tempo = channelParameter[ chn ];
					}
					else if( channelParameter[ chn ] > 0 )
					{
						currentTick = ticksPerRow = channelParameter[ chn ];
					}
					break;
				case 0xE0: /* Set filter. */
					break;
				case 0xE1: /* Fine portamento up. */
					channelPeriod[ chn ] -= parameter;
					break;
				case 0xE2: /* Fine portamento down. */
					channelPeriod[ chn ] += parameter;
					break;
				case 0xE3: /* Glissando. */
				case 0xE4: /* Set vibrato waveform. */
					break;
				case 0xE5: /* Set finetune. */
					sampleFineTune[ channelAssigned[ chn ] ] = parameter;
					break;
				case 0xE6: /* Pattern loop. */
					if( channelPatternLoopRow[ chn ] < currentRow )
					{
						if( parameter > 0 )
						{
							if( patternLoopCount <= 0 )
							{
								patternLoopCount = parameter + 1;
								patternLoopChannel = chn;
							}
							if( patternLoopChannel == chn )
							{
								patternLoopCount--;
								if( patternLoopCount > 0 )
								{
									nextSequencePos = currentSequencePos;
									nextRow = channelPatternLoopRow[ chn ];
									patternBreak = false;
								}
								else
								{
									channelPatternLoopRow[ chn ] = currentRow + 1;
								}
							}
						}
						else
						{
							channelPatternLoopRow[ chn ] = currentRow;
						}
					}
					break;
				case 0xE7: /* Set tremolo waveform. */
				case 0xE8: /* Panning. */
				case 0xE9: /* Retrig. */
					break;
				case 0xEA: /* Fine volume slide up. */
					channelVolume[ chn ] += parameter;
					break;
				case 0xEB: /* Fine volume slide down. */
					channelVolume[ chn ] -= parameter;
					break;
				case 0xEC: /* Note cut. */
					break;
				case 0xED: /* Note delay. */
					if( period <= 0 )
					{
						channelParameter[ chn ] = 0;
					}
					break;
				case 0xEE: /* Pattern delay. */
					currentTick += ticksPerRow * parameter;
					break;
				case 0xEF: /* Invert loop. */
					break;
			}
		}
		if( patternBreak )
		{
			patternLoopCount = 0;
			patternLoopChannel = 0;
			for( int chn = 0; chn < numChannels; chn++ )
			{
				channelPatternLoopRow[ chn ] = 0;
			}
		}
	}
	
	private void tick()
	{
		if( --currentTick <= 0 )
		{
			row();
			effectCounter = 0;
		}
		else
		{
			effectCounter++;
			for( int chn = 0; chn < numChannels; chn++ ) 
			{
				switch( channelEffect[ chn ] )
				{
					case 0x0: /* Arpeggio. */
						if( channelParameter[ chn ] > 0 )
						{
							switch( effectCounter % 3 )
							{
								default:
									channelArpeggio[ chn ] = 0;
									break;
								case 1:
									channelArpeggio[ chn ] = ( channelParameter[ chn ] >> 4 ) & 0xF;
									break;
								case 2:
									channelArpeggio[ chn ] = channelParameter[ chn ] & 0xF;
									break;
							}
						}
						break;
					case 0x1: /* Portamento up. */
						channelPeriod[ chn ] -= channelParameter[ chn ];
						break;
					case 0x2: /* Portamento down. */
						channelPeriod[ chn ] += channelParameter[ chn ];
						break;
					case 0x3: /* Tone portamento. */
						tonePortamento( chn );
						break;
					case 0x4: /* Vibrato. */
						vibrato( chn );
						break;
					case 0x5: /* Tone porta + Volume slide. */
						tonePortamento( chn );
						volumeSlide( chn );
						break;
					case 0x6: /* Vibrato + Volume slide. */
						vibrato( chn );
						volumeSlide( chn );
						break;
					case 0x7: /* Tremolo. */
						tremolo( chn );
						break;
					case 0xA: /* Volume slide. */
						volumeSlide( chn );
						break;
					case 0xE9: /* Retrig. */
						if( effectCounter % channelParameter[ chn ] == 0 )
						{
							channelSamplePos[ chn ] = 0;
						}
						break;
					case 0xEC: /* Note cut. */
						if( effectCounter == channelParameter[ chn ] )
						{
							channelVolume[ chn ] = 0;
						}
						break;
					case 0xED: /* Note delay. */
						if( effectCounter == channelParameter[ chn ] )
						{
							channelInstrument[ chn ] = channelAssigned[ chn ];
							channelPeriod[ chn ] = channelPortaPeriod[ chn ];
							channelSamplePos[ chn ] = 0;
							channelVibratoPhase[ chn ] = 0;
						}
						break;
				}
			}
		}
		for( int chn = 0; chn < numChannels; chn++ ) 
		{
			/* Calculate volume and frequency. */
			int volume = channelVolume[ chn ];
			if( volume > 64 )
			{
				volume = channelVolume[ chn ] = 64;
			}
			else if( volume < 0 )
			{
				volume = channelVolume[ chn ] = 0;
			}
			volume = volume + channelTremolo[ chn ];
			if( volume > 64 )
			{
				volume = 64;
			}
			else if( volume < 0 )
			{
				volume = 0;
			}
			int period = channelPeriod[ chn ];
			if( period > 0 )
			{
				period = period + channelVibrato[ chn ];
				period = ( period * ARPEGGIO[ channelArpeggio[ chn ] ] ) >> FIXED_POINT_SHIFT;
				if( period < 28 )
				{
					period = 28;
				}
				int frequency = c2Rate * 428 / period;
				int fineTune = sampleFineTune[ channelInstrument[ chn ] ];
				frequency = ( frequency * FINE_TUNE[ fineTune ] ) >> FIXED_POINT_SHIFT;
				channelStep[ chn ] = frequency * FIXED_POINT_ONE / sampleRate;
			}
		}
	}
	
	private void tonePortamento( int chn )
	{
		if( channelPeriod[ chn ] < channelPortaPeriod[ chn ] )
		{
			channelPeriod[ chn ] += channelPortaSpeed[ chn ];
			if( channelPeriod[ chn ] > channelPortaPeriod[ chn ] )
			{
				channelPeriod[ chn ] = channelPortaPeriod[ chn ];
			}
		}
		else
		{
			channelPeriod[ chn ] -= channelPortaSpeed[ chn ];
			if( channelPeriod[ chn ] < channelPortaPeriod[ chn ] )
			{
				channelPeriod[ chn ] = channelPortaPeriod[ chn ];
			}
		}
	}
	
	private void vibrato( int chn )
	{
		int phase = channelVibratoPhase[ chn ] & 0x3F;
		channelVibrato[ chn ] = ( VIBRATO[ phase & 0x1F ] * channelVibratoDepth[ chn ] ) >> 7;
		if( phase > 0x1F )
		{
			channelVibrato[ chn ] = -channelVibrato[ chn ];
		}
		channelVibratoPhase[ chn ] += channelVibratoSpeed[ chn ];
	}
	
	private void volumeSlide( int chn )
	{
		int up = ( channelParameter[ chn ] >> 4 ) & 0xF;
		int down = channelParameter[ chn ] & 0xF;
		channelVolume[ chn ] += up - down;
	}
	
	private void tremolo( int chn )
	{
		int phase = channelVibratoPhase[ chn ] & 0x3F;
		channelTremolo[ chn ] = ( VIBRATO[ phase & 0x1F ] * channelTremoloDepth[ chn ] ) >> 6;
		if( phase > 0x1F )
		{
			channelTremolo[ chn ] = -channelTremolo[ chn ];
		}
		channelVibratoPhase[ chn ] += channelTremoloSpeed[ chn ];
	}
	
	private static byte[] readBytes( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = new byte[ length ];
		int offset = 0;
		int count = 0;
		while( offset < length && count >= 0 )
		{
			offset += count;
			count = inputStream.read( bytes, offset, length - offset );
		}
		return bytes;
	}
	
	private static String readString( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = readBytes( inputStream, length );
		length = 0;
		for( int idx = 0; idx < bytes.length; idx++ )
		{
			if( ( bytes[ idx ] & 0xFF ) <= 32 )
			{
				bytes[ idx ] = 32;
			}
			else
			{
				length = idx + 1;
			}
		}
		return new String( bytes, 0, length, "ISO-8859-1" );
	}
	
	private static void downsample( int[] buf, int count )
	{
		for( int idx = 0; idx < count; idx++ )
		{
			int lamp = 0;
			int ramp = 0;
			for( int coef = 0; coef < FILTER_COEFFS.length; coef++ )
			{
				lamp += ( buf[ idx * 4 + coef * 2 ] * FILTER_COEFFS[ coef ] ) >> FIXED_POINT_SHIFT;
				ramp += ( buf[ idx * 4 + coef * 2 + 1 ] * FILTER_COEFFS[ coef ] ) >> FIXED_POINT_SHIFT;
			}
			buf[ idx * 2 ] = lamp;
			buf[ idx * 2 + 1 ] = ramp;
		}
	}
	
	private static String pad( String string, int length, char chr, boolean left )
	{
		if( string.length() < length )
		{
			char[] chars = new char[ length ];
			for( int idx = 0; idx < chars.length; idx++ )
			{
				chars[ idx ] = chr;
			}
			string.getChars( 0, string.length(), chars, left ? length - string.length() : 0 );
			return new String( chars );
		}
		return string;
	}
	
	public static void main( String[] args ) throws Exception {
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, ( idx - 8 ) / 96.0 ) * FIXED_POINT_ONE ) );
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, idx / -12.0 ) * FIXED_POINT_ONE ) );
		ModPlay3 modPlay3 = null;
		java.io.InputStream inputStream = new java.io.FileInputStream( args[ 0 ] );
		try
		{
			modPlay3 = new ModPlay3( 96000, inputStream, false );
		}
		catch( IllegalArgumentException e )
		{
			System.out.println( e.getMessage() + " Assuming Ultimate Soundtracker format." );
		}
		finally
		{
			inputStream.close();
		}
		if( modPlay3 == null )
		{
			inputStream = new java.io.FileInputStream( args[ 0 ] );
			try
			{
				modPlay3 = new ModPlay3( 96000, inputStream, true );
			}
			finally
			{
				inputStream.close();
			}
		}
		System.out.println( "ModPlay3 (C)2018 Martin Cameron!" );
		System.out.println( "Playing: " + pad( modPlay3.songName, 20, ' ', false ) + " Len   Loop" );
		for( int idx = 1; idx < MAX_SAMPLES && modPlay3.instrumentNames[ idx ] != null; idx++ )
		{
			if( modPlay3.sampleData[ idx ].length > 0 || modPlay3.instrumentNames[ idx ].length() > 0 )
			{
				int loop = modPlay3.sampleLoopLength[ idx ] >> FIXED_POINT_SHIFT;
				int len = ( modPlay3.sampleLoopStart[ idx ] >> FIXED_POINT_SHIFT ) + loop;
				System.out.println( pad( String.valueOf( idx ), 2, '0', true ) + ' '
					+ pad( modPlay3.instrumentNames[ idx ], 23, ' ', false )
					+ pad( String.valueOf( len ), 7, ' ', true ) + pad( String.valueOf( loop ), 7, ' ', true ) );
			}
		}
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat( 48000, 16, 2, true, false );
		javax.sound.sampled.SourceDataLine sourceDataLine = ( javax.sound.sampled.SourceDataLine )
			javax.sound.sampled.AudioSystem.getLine( new javax.sound.sampled.DataLine.Info(
				javax.sound.sampled.SourceDataLine.class, audioFormat ) );
		sourceDataLine.open( audioFormat );
		sourceDataLine.start();
		final int MIX_BUF_SAMPLES = 2048;
		int[] mixBuf = new int[ ( MIX_BUF_SAMPLES + FILTER_COEFFS.length ) * 2 ];
		byte[] outBuf = new byte[ MIX_BUF_SAMPLES * 2 ];
		while( true )
		{
			System.arraycopy( mixBuf, MIX_BUF_SAMPLES * 2, mixBuf, 0, FILTER_COEFFS.length * 2 );
			modPlay3.getAudio( mixBuf, FILTER_COEFFS.length, MIX_BUF_SAMPLES );
			downsample( mixBuf, MIX_BUF_SAMPLES / 2 );
			for( int idx = 0; idx < MIX_BUF_SAMPLES; idx++ )
			{
				int ampl = mixBuf[ idx ];
				if( ampl > 32767 )
				{
					ampl = 32767;
				}
				if( ampl < -32768 )
				{
					ampl = -32768;
				}
				outBuf[ idx * 2 ] = ( byte ) ampl;
				outBuf[ idx * 2 + 1 ] = ( byte ) ( ampl >> 8 );
			}
			sourceDataLine.write( outBuf, 0, outBuf.length );
		}
	}
}
